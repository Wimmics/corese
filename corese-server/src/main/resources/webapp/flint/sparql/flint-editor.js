// Flint Sparql Editor

// Written by Stephen Cresswell and Paul Appleby

function FlintEditor(container, imagesPath, config) {

	"use strict";

	var editor;

	if (config.endpoints === null) {
		window.alert("There must be at least one endpoint defined");
		return;
	}

	function FlintError(editor) {

		this.show = function(message) {
			try {

				editor.getConfirmDialog().setCloseAction();
				editor.getConfirmDialog().show("Flint Error",
						"<p>" + message.toString() + "</p>", true);
			} catch (e) {
				window.alert(e);
			}

		};
	}

	function FlintAbout(editor) {

		this.show = function() {
			var aboutText = "<p>"
					+ editor.getTitle()
					+ ", version "
					+ editor.getVersion()
					+ "</p>"
					+ "<p>Flint uses Marijn Haverbeke's <a href='http://codemirror.net/'>CodeMirror 2</a>.</p>"
					+ "<p>Flint has been developed by a team at <a href='http://www.tso.co.uk'>TSO</a>.</p>";
			editor.getConfirmDialog().setCloseAction();
			editor.getConfirmDialog().show("About Flint", aboutText, true);
		};
	}

	function FlintDialog() {

		var button = "";
		var closeAction = {};

		this.show = function(title, text, closeOnly) {

			if (!editor.windowClosing) {
				$('#flint-dialog-title-text').text(title);
				$('#flint-dialog-text').html(text);
				if (closeOnly) {
					$('#flint-dialog-okay-button').css('visibility', 'hidden');
				} else {
					$('#flint-dialog-okay-button').css('visibility', 'visible');
				}
				$('.flint-dialog-body').css('margin-top',
						($('#flint-editor').position().top + 200) + "px");
				$('#flint-dialog').css('visibility', 'visible');
			}
		};

		this.getResult = function() {
			return button;
		};

		this.setCloseAction = function(callback) {
			if (callback !== null) {
				closeAction = callback;
			} else {
				closeAction = function() {
				};
			}
		};

		this.display = function(container) {
			var aboutText = "<div id='flint-dialog'' class='flint-dialog'><div class='flint-dialog-body'><div class='flint-dialog-body-container'><h2 id='flint-dialog-title'><span id='flint-dialog-close' class='flint-close'></span><span id='flint-dialog-title-text'>Title goes here</span></h2>"
					+ "<div id='flint-dialog-text'></div>"
					+ "<div id='flint-dialog-buttons'><span id='flint-dialog-close-button' class='flint-close-button''>Close</span><span id='flint-dialog-okay-button' class='flint-okay-button'>Okay</span></div>"
					+ "</div></div></div>";
			$('#' + container).append(aboutText);

			$('#flint-dialog-close').click(function() {
				$('#flint-dialog-okay-button').css('visibility', 'hidden');
				$('#flint-dialog').css('visibility', 'hidden');
				button = "Close";
				closeAction();
			});
			$('#flint-dialog-okay-button').click(function() {
				try {
					$('#flint-dialog-okay-button').css('visibility', 'hidden');
					$('#flint-dialog').css('visibility', 'hidden');
					button = "Okay";
					closeAction();
				} catch (e) {
					editor.getErrorBox().show(e);
				}
			});
			$('#flint-dialog-close-button').click(function() {
				$('#flint-dialog-okay-button').css('visibility', 'hidden');
				$('#flint-dialog').css('visibility', 'hidden');
				button = "Close";
				closeAction();
			});
		};
	}

	function FlintStatus() {

		var line = 0;
		var position = 0;
		var queryValid = "valid";

		this.setLine = function(cursorLine) {
			line = cursorLine;
		};

		this.setQueryValid = function(valid) {
			if (valid) {
				queryValid = "valid";
			} else {
				queryValid = "invalid";
			}
		};

		this.setPosition = function(cursorPosition) {
			position = cursorPosition;
		};

		this.display = function(container) {
			$('#' + container).append("<div id='flint-status'>...</div>");
		};

		this.updateStatus = function() {
			$('#flint-status').text(
					"Line: " + (line + 1) + "; Position: " + (position + 1)
							+ "; Query is " + queryValid);
		};
	}

	function FlintResults(editor) {

		var results = "";
		var resultsMode = "Visual";

		this.setResults = function(text) {
			results = text;
			if ($.isXMLDoc(results)) {
				if (!window.XMLSerializer) {
					results = results.xml;
				} else {
					var serializer = new window.XMLSerializer();
					results = serializer.serializeToString(results);
				}
			}
			// It's SPARQL XML and we're in dev mode
			if (resultsMode === "Visual"
					&& results.indexOf("http://www.w3.org/2005/sparql-results#") > 0) {
				var devHead = "";
				var devResults = "";
				$(results).find("variable").each(function() {
					devHead += "<th>" + $(this).attr("name") + "</th>";
				});
				$(results).find("result").each(
						function() {
							devResults += "<tr>";
							var resultItem = $(this);
							$(results).find("variable").each(
									function() {
										resultItem.find(
												"binding[name='" + $(this).attr("name") + "']").each(
												function() {
													$(this).find("*").each(
															function() {
																if (this.tagName === "URI") {
																	devResults += "<td><a href='"
																			+ $(this).text() + "'>" + $(this).text()
																			+ "</a></td>";
																} else {
																	devResults += "<td>" + $(this).text()
																			+ "</td>";
																}
															});
												});
									});
							devResults += "</tr>";
						});
				results = "<table id='flint-results-table'><thead><tr>" + devHead
						+ "</tr></thead><tbody>" + devResults + "</tbody></table>";
			} else {
				results = "<textarea id='flint-results'>" + results + "</textarea>";
			}
			this.showLoading(false);
			try {
				$('#flint-results-container').html(results);
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.getResults = function() {
			return results;
		};

		this.showLoading = function(showLoader) {
			if (showLoader) {
				$('#flint-results-loader').show();
				$('#flint-results').hide();
			} else {
				$('#flint-results-loader').hide();
				$('#flint-results').show();
			}
		};

		// Indicates whether results should be basic as returned by server of
		// enhanced
		this.getResultsMode = function() {
			return resultsMode;
		};

		this.display = function(container) {
			$('#' + container)
					.append(
							"<h2 id='flint-results-heading'>Query Results<span id='flint-results-mode' title='Toggle between visually enhanced results or basic text format'>Visual Results Mode</span></h2>");
			$('#' + container)
					.append(
							"<div id='flint-results-area';><p id='flint-results-loader'><img src='"
									+ editor.getImagesPath()
									+ "/ajax-loader-red.gif'/> Running query ... please wait</p>"
									+ "<div id='flint-results-container'><textarea id='flint-results'></textarea></div></div>");
			$('#flint-results-mode').click(function() {
				if ($(this).text() === "Visual Results Mode") {
					$(this).text("Basic Results Mode");
					resultsMode = "Basic";
				} else {
					$(this).text("Visual Results Mode");
					resultsMode = "Visual";
				}
			});
		};
	}

	function FlintMenuItem(itemId, itemLabel, itemIcon, itemEnabled, itemCallback) {

		var id = itemId;
		var subMenu = null;
		var icon = itemIcon;
		var callback = itemCallback;
		var enabled = itemEnabled;

		this.getId = function() {
			return id;
		};
		this.getIcon = function() {
			return icon;
		};
		this.getLabel = function() {
			return itemLabel;
		};
		this.setSubMenu = function(menu) {
			subMenu = menu;
		};
		this.getSubMenu = function() {
			return subMenu;
		};
		this.getCallback = function() {
			return callback;
		};
		this.setEnabled = function(value) {
			enabled = value;
		};
		this.getEnabled = function() {
			return enabled;
		};
	}

	function FlintMenu(editor) {

		var menuItems = [];
		var newMenuItems = [];

		try {
			newMenuItems.push(new FlintMenuItem("NewTab", "New Query Tab",
					"NewTab_16x16.png", true, function() {
						editor.addTab();
					}));
			newMenuItems.push(new FlintMenuItem("EmptyQuery", "Empty Query",
					"New_16x16.png", true, function() {
						editor.clearEditorTextArea();
					}));
			newMenuItems.push(new FlintMenuItem("SelectQuery", "Select",
					"Properties_16x16.png", true, function() {
						editor.insertSelectQuery();
					}));
			newMenuItems.push(new FlintMenuItem("ConstructQuery", "Construct",
					"Key_16x16.png", true, function() {
						editor.insertConstructQuery();
					}));
			newMenuItems.push(new FlintMenuItem("InsertQuery", "Insert",
					"Insert_16x16.png", true, function() {
						editor.insertInsertQuery();
					}));
			newMenuItems.push(new FlintMenuItem("DeleteQuery", "Delete",
					"DeleteQuery_16x16.png", true, function() {
						editor.insertDeleteQuery();
					}));

			var editMenuItems = [];
			editMenuItems.push(new FlintMenuItem("Undo", "Undo", "Undo_16x16.png",
					false, function() {
						editor.undo();
					}));
			editMenuItems.push(new FlintMenuItem("Redo", "Redo", "Redo_16x16.png",
					false, function() {
						editor.redo();
					}));
			editMenuItems.push(new FlintMenuItem("Cut", "Cut", "Cut_16x16.png",
					false, function() {
						editor.cut();
					}));

			var viewMenuItems = [];
			viewMenuItems.push(new FlintMenuItem("Show Tools", "Show Tools Pane",
					"Prev_16x16.png", true, function() {
						editor.toggleTools();
					}));
			viewMenuItems.push(new FlintMenuItem("Hide Tools", "Hide Tools Pane",
					"Next_16x16.png", false, function() {
						editor.toggleTools();
					}));
			viewMenuItems.push(new FlintMenuItem("Show Endpoints",
					"Show Endpoints Bar", "Globe_16x16.png", true, function() {
						editor.showEndpointBar();
					}));
			viewMenuItems.push(new FlintMenuItem("Show Datasets",
					"Show Datasets Bar", "Favorites_16x16.png", false, function() {
						editor.showDataSetsBar();
					}));

			var helpMenuItems = [];
			helpMenuItems.push(new FlintMenuItem("About", "About",
					"Information_16x16.png", true, function() {
						editor.showAbout();
					}));

			var newMenuItem = new FlintMenuItem("New", "New", true);
			newMenuItem.setSubMenu(newMenuItems);
			menuItems.push(newMenuItem);

			var editMenuItem = new FlintMenuItem("Edit", "Edit", true);
			editMenuItem.setSubMenu(editMenuItems);
			menuItems.push(editMenuItem);

			var viewMenuItem = new FlintMenuItem("View", "View", true);
			viewMenuItem.setSubMenu(viewMenuItems);
			menuItems.push(viewMenuItem);

			var helpMenuItem = new FlintMenuItem("Help", "Help", true);
			helpMenuItem.setSubMenu(helpMenuItems);
			menuItems.push(helpMenuItem);

			this.getItems = function() {
				return menuItems;
			};
		} catch (e) {
			editor.getErrorBox().show(e);
		}

		this.setEnabled = function(id, enabled) {
			var i;
			var j;
			for (i = 0; i < menuItems.length; i++) {
				if (menuItems[i].getSubMenu() !== null) {
					for (j = 0; j < menuItems[i].getSubMenu().length; j++) {
						if (menuItems[i].getSubMenu()[j].getId() === id) {
							menuItems[i].getSubMenu()[j].setEnabled(enabled);
							if (enabled) {
								$("#flint-submenu-item-" + i + "-" + j).attr("class",
										"flint-menu-enabled");
							} else {
								$("#flint-submenu-item-" + i + "-" + j).attr("class",
										"flint-menu-disabled");
							}
							break;
						}
					}
				}
			}
		};

		this.display = function(container) {
			var listItems = "";
			var i;
			var j;
			for (i = 0; i < menuItems.length; i++) {
				listItems += "<li class='flint-menu-item' id='flint-menu-" + i
						+ "'><span>";
				listItems += menuItems[i].getLabel();
				listItems += "</span>";
				if (menuItems[i].getSubMenu() !== null) {
					var subList = "";
					for (j = 0; j < menuItems[i].getSubMenu().length; j++) {
						subList += "<li class='";
						if (menuItems[i].getSubMenu()[j].getEnabled()) {
							subList += "flint-menu-enabled";
						} else {
							subList += "flint-menu-disabled";
						}
						subList += "' id='flint-submenu-item-" + i + "-" + j + "'><span>";
						if (menuItems[i].getSubMenu()[j].getIcon() !== "") {
							subList += "<img src='" + editor.getImagesPath() + "/"
									+ menuItems[i].getSubMenu()[j].getIcon() + "'/>";
						} else {
							subList += "<span class='flint-menu-filler'></span>";
						}
						subList += menuItems[i].getSubMenu()[j].getLabel();
						subList += "</span></li>";
					}
					listItems += "<ul class='flint-submenu' id='flint-submenu-" + i
							+ "'>" + subList + "</ul>";
				}
				listItems += "</li>";
			}
			$('#' + container).append("<ul id='flint-menu'>" + listItems + "</ul>");

			// Now add events
			for (i = 0; i < menuItems.length; i++) {
				if (menuItems[i].getSubMenu() !== null) {
					for (j = 0; j < menuItems[i].getSubMenu().length; j++) {
						$("#flint-submenu-item-" + i + "-" + j).click(
								menuItems[i].getSubMenu()[j].getCallback());
					}
				}
			}
		};
	}

	function FlintToolbarItem(itemId, itemLabel, itemIcon, itemEnabled,
			itemCallback, itemStartGroup) {

		var id = itemId;
		var label = itemLabel;
		var icon = itemIcon;
		var callback = itemCallback;
		var enabled = itemEnabled;
		var startGroup = itemStartGroup;

		this.getId = function() {
			return id;
		};
		this.getLabel = function() {
			return label;
		};
		this.getIcon = function() {
			return icon;
		};
		this.getCallback = function() {
			return callback;
		};
		this.setEnabled = function(value) {
			enabled = value;
		};
		this.getEnabled = function() {
			return enabled;
		};
		this.getStartGroup = function() {
			return startGroup;
		};
	}

	function FlintToolbar(editor) {

		var toolbarItems = [];

		try {
			toolbarItems.push(new FlintToolbarItem("New Query Tab", "New Query Tab",
					"NewTab_24x24.png", true, function() {
						editor.addTab();
					}, false));
			toolbarItems.push(new FlintToolbarItem("New", "New empty query",
					"New_24x24.png", true, function() {
						editor.clearEditorTextArea();
					}, true));
			toolbarItems.push(new FlintToolbarItem("Select", "New select query",
					"Properties_24x24.png", true, function() {
						editor.insertSelectQuery();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Construct",
					"New construct query", "Key_24x24.png", true, function() {
						editor.insertConstructQuery();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Insert", "New insert query",
					"Insert_24x24.png", true, function() {
						editor.insertInsertQuery();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Delete", "New delete query",
					"DeleteQuery_24x24.png", true, function() {
						editor.insertDeleteQuery();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Undo", "Undo last edit",
					"Undo_24x24.png", false, function() {
						editor.undo();
					}, true));
			toolbarItems.push(new FlintToolbarItem("Redo", "Redo last edit",
					"Redo_24x24.png", false, function() {
						editor.redo();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Cut", "Cut selected text",
					"Cut_24x24.png", false, function() {
						editor.cut();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Show Tools", "Show tools pane",
					"Prev_24x24.png", true, function() {
						editor.toggleTools();
					}, true));
			toolbarItems.push(new FlintToolbarItem("Hide Tools", "Hide tools pane",
					"Next_24x24.png", false, function() {
						editor.toggleTools();
					}, false));
			toolbarItems.push(new FlintToolbarItem("Show Endpoints",
					"Show endpoints bar", "Globe_24x24.png", true, function() {
						editor.showEndpointBar();
					}, true));
			toolbarItems.push(new FlintToolbarItem("Show Datasets",
					"Show datasets bar", "Favorites_24x24.png", false, function() {
						editor.showDataSetsBar();
					}, false));
			// toolbarItems.push(new FlintToolbarItem("FR", "Find/Replace",
			// "Find_24x24.png", true, function() {editor.cut()}));
			this.getItems = function() {
				return toolbarItems;
			};
		} catch (e) {
			editor.getErrorBox().show(e);
		}

		// This is probably a bit inefficient. Need to find a better way
		this.setEnabled = function(id, enabled) {
			var i;
			for (i = 0; i < toolbarItems.length; i++) {
				if (toolbarItems[i].getId() === id) {
					toolbarItems[i].setEnabled(enabled);
					var itemClass = "";
					if (enabled) {
						itemClass = "flint-toolbar-enabled";
					} else {
						itemClass = "flint-toolbar-disabled";
					}
					if (toolbarItems[i].getStartGroup()) {
						itemClass += " flint-toolbar-start";
					}
					$("#flint-toolbar-" + i).attr("class", itemClass);
					break;
				}
			}
		};

		this.display = function(container) {
			var listItems = "";
			var i;
			for (i = 0; i < toolbarItems.length; i++) {
				listItems += "<li id='flint-toolbar-" + i + "' class='";
				if (toolbarItems[i].getEnabled()) {
					listItems += "flint-toolbar-enabled";
				} else {
					listItems += "flint-toolbar-disabled";
				}
				if (toolbarItems[i].getStartGroup()) {
					listItems += " flint-toolbar-start";
				}
				listItems += "'><img src='" + editor.getImagesPath() + "/"
						+ toolbarItems[i].getIcon() + "' title='"
						+ toolbarItems[i].getLabel() + "' alt='"
						+ toolbarItems[i].getLabel() + "'/></li>";
			}
			$('#' + container)
					.append("<ul id='flint-toolbar'>" + listItems + "</ul>");
			for (i = 0; i < toolbarItems.length; i++) {
				$("#flint-toolbar-" + i).click(toolbarItems[i].getCallback());
			}
		};
	}

	// The endpoint entry item allows for a freeform URL of an endpoint
	function FlintEndpointEntry(config, editor) {

		try {
			var endpointItems = [];

			this.addItem = function() {
				try {
					var i;
					for (i = 0; i < endpointItems.length; i++) {
						if (endpointItems[i].uri === this.getEndpoint()) {
							return;
						}
					}
					var newItem = {};
					newItem.uri = this.getEndpoint();
					endpointItems.push(newItem);
				} catch (e) {
					editor.getErrorBox().show("EndpointEntryAddItem: " + e);
				}
			};

			this.getItems = function() {
				return endpointItems;
			};

			this.getItem = function(endpoint) {
				var i;
				for (i = 0; i < endpointItems.length; i++) {
					if (endpointItems[i].uri === endpoint) {
						return endpointItems[i];
					}
				}
				return null;
			};

			this.display = function(container) {
				var endpoint = 'http://gov.tso.co.uk/tso-gazette-index-wwi/sparql';
				$('#' + container)
						.append(
								"<div id='flint-endpoint-input' title='Enter the endpoint that you wish to query'><h2>Endpoint</h2><input id='flint-endpoint-url' type='text' value='"
										+ endpoint + "' name='endpoint'></div>");
				// Ensure we register the endpoint
				this.addItem();
			};

			this.getEndpoint = function() {
				return $("#flint-endpoint-url").val();
			};
		} catch (e) {
			editor.getErrorBox().show("FlintEndpointEntry: " + e);
		}
	}

	// The SPARQL mode picker allows a user to select
	function FlintModePicker(config, editor, pickerContext) {

		try {
			var modeItems = [];
			var i;
			// config.modes contains the list of SPARQL modes that should be
			// made available
			for (i = 0; i < config.defaultModes.length; i++) {
				modeItems.push(config.defaultModes[i]);
			}

			this.getModes = function() {
				return modeItems;
			};

			// Expects an array of possible modes and updates the select
			// dropdown
			this.updateModes = function(datasetItem) {
				$("#flint-" + pickerContext + "-mode-select").text("");
				var index;
				for (index = 0; index < modeItems.length; index++) {
					var listItem = "<option id='flint-mode-" + modeItems[index].mode
							+ "' value='" + modeItems[index].mode + "'>"
							+ modeItems[index].name + "</option>";
					if (datasetItem.modes) {
						var i;
						for (i = 0; i < datasetItem.modes.length; i++) {
							if (datasetItem.modes[i] === modeItems[index].mode) {
								$("#flint-" + pickerContext + "-mode-select").append(listItem);
							}
						}
					} else {
						$("#flint-" + pickerContext + "-mode-select").append(listItem);
					}
				}
				$("#flint-" + pickerContext + "-mode-select option:first").change();
			};

			this.display = function(container) {
				var listItems = "";

				// if only 1 mode, display disabled textbox instead of
				// dropdown
				if (modeItems.length === 1) {
					$('#' + container)
							.append(
									"<div id='flint-"
											+ pickerContext
											+ "-modes'><h2>Mode</h2><input disabled='disabled' type=text id='flint-"
											+ pickerContext + "-mode-select' name='mode' value='"
											+ modeItems[0].name + "' /></div>");
				} else {
					var i;
					for (i = 0; i < modeItems.length; i++) {
						listItems += "<option id='flint-mode-" + modeItems[i].mode
								+ "' value='" + modeItems[i].mode + "'>" + modeItems[i].name
								+ "</option>";
					}
					$('#' + container)
							.append(
									"<div id='flint-"
											+ pickerContext
											+ "-modes' title='Select the SPARQL mode'><h2>Mode</h2><select id='flint-"
											+ pickerContext + "-mode-select' name='mode'>"
											+ listItems + "</select></div>");
				}
			};

			this.getMode = function() {
				return $("#flint-" + pickerContext + "-mode-select").val();
			};

			this.setChangeAction = function(callback) {
				$('#flint-' + pickerContext + '-mode-select').change(callback);
			};
		} catch (e) {
			editor.getErrorBox().show(e);
		}
	}

	// The dataset picker allows a user to select the endpoint that they wish to
	// send queries to
	function FlintDatasetPicker(config, editor) {

		try {
			var datasetItems = [];

			// config.endpoints contains the list of endpoints that should be
			// made available and thus their corresponding configuration data
			var i;
			for (i = 0; i < config.endpoints.length; i++) {
				datasetItems.push(config.endpoints[i]);
			}

			this.getItems = function() {
				return datasetItems;
			};

			this.getItem = function(endpoint) {
				var i;
				for (i = 0; i < datasetItems.length; i++) {
					if (datasetItems[i].uri === endpoint) {
						return datasetItems[i];
					}
				}
			};

			this.display = function(container) {
				var listItems = "";

				// if only 1 dataset, display disabled textbox instead of
				// dropdown
				if (datasetItems.length === 1) {
					$('#' + container)
							.append(
									"<div id='flint-dataset'><h2>Dataset</h2><input disabled='disabled' type=text id='flint-dataset-select' name='kb' value='"
											+ datasetItems[0].uri + "' /></div>");
				} else {
					var i;
					for (i = 0; i < datasetItems.length; i++) {
						listItems += "<option value='" + datasetItems[i].uri + "'>"
								+ datasetItems[i].name + "</option>";
					}
					$('#' + container)
							.append(
									"<div id='flint-dataset' title='Select the endpoint that you wish to query'><h2>Dataset</h2><select id='flint-dataset-select' name='kb'>"
											+ listItems + "</select></div>");
				}
			};

			this.getEndpoint = function() {
				return $("#flint-dataset-select").val();
			};

			this.setChangeAction = function(callback) {
				$('#flint-dataset-select').change(callback);
			};
		} catch (e) {
			editor.getErrorBox().show(e);
		}
	}

	function FlintEndpointDataInfoButton(editor) {

		this.display = function(container) {
			try {
				$('#' + container)
						.append(
								"<input class='flint-info-button' id='flint-endpoint-datainfo' type='button' value='Get Dataset Info' title='Query dataset for properties and classes'/>");
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.setClickAction = function(callback) {
			$('#flint-endpoint-datainfo').click(callback);
		};
	}

	function FlintEndpointQuerySubmitButton(editor) {

		this.disable = function() {
			$('.flint-submit-button').css('visibility', 'hidden');
		};

		this.enable = function() {
			$('.flint-submit-button').css('visibility', 'visible');
		};

		this.display = function(container) {
			try {
				$('#' + container)
						.append(
								"<input class='flint-submit-button' id='flint-endpoint-submit' type='submit' value='Submit' title='Submit query to endpoint'/>");
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.setSubmitAction = function(callback) {
			$('#flint-endpoint-submit').click(callback);
		};
	}

	// Submit button for queries from the dataset list
	function FlintDatasetQuerySubmitButton(editor) {

		this.disable = function() {
			$('.flint-submit-button').css('visibility', 'hidden');
		};

		this.enable = function() {
			$('.flint-submit-button').css('visibility', 'visible');
		};

		this.display = function(container) {
			try {
				$('#' + container)
						.append(
								"<input class='flint-submit-button' id='flint-dataset-submit' type='submit' value='Submit' title='Submit query to endpoint'/>");
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.setSubmitAction = function(callback) {
			$('#flint-dataset-submit').click(callback);
		};
	}

	function FlintEndpointMimeTypePicker(config, editor) {

		this.setQueryType = function(queryType) {
			try {
				if (queryType === "SELECT") {
					$('#flint-endpoint-mimeset-select-chooser').show();
					$('#flint-endpoint-mimeset-construct-chooser').hide();

					if ($('#flint-endpoint-bar').is(':visible')) {
						$('#flint-endpoint-mimeset-select').attr('disabled', '');
						$('#flint-endpoint-mimeset-construct').attr('disabled', 'disabled');
					} else {
						$(
								'#flint-endpoint-mimeset-select, #flint-endpoint-mimeset-construct')
								.attr('disabled', 'disabled');
					}
				} else if (queryType === "CONSTRUCT" || queryType === "DESCRIBE") {
					$('#flint-endpoint-mimeset-construct-chooser').show();
					$('#flint-endpoint-mimeset-select-chooser').hide();

					if ($('#flint-endpoint-bar').is(':visible')) {
						$('#flint-endpoint-mimeset-construct').attr('disabled', '');
						$('#flint-endpoint-mimeset-select').attr('disabled', 'disabled');
					} else {
						$(
								'#flint-endpoint-mimeset-select, #flint-endpoint-mimeset-construct')
								.attr('disabled', 'disabled');
					}
				}
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.display = function(container) {
			try {
				var selectChooser = "";
				var constructChooser = "";

				// use output parameter for IE, otherwise accept header mimetype
				var type;
				if ($.browser.msie) {
					type = 'format';
				} else {
					type = 'type';
				}

				var i;
				for (i = 0; i < config.defaultEndpointParameters.selectFormats.length; i++) {
					selectChooser += "<option value='"
							+ config.defaultEndpointParameters.selectFormats[i][type] + "'>"
							+ config.defaultEndpointParameters.selectFormats[i].name
							+ "</option>";
				}

				for (i = 0; i < config.defaultEndpointParameters.constructFormats.length; i++) {
					constructChooser += "<option value='"
							+ config.defaultEndpointParameters.constructFormats[i][type]
							+ "'>"
							+ config.defaultEndpointParameters.constructFormats[i].name
							+ "</option>";
				}

				$('#' + container)
						.append(
								"<div id='flint-endpoint-output-formats' title='Select the format in which you would like the results to be returned'><h2>Output</h2></div>");

				selectChooser = "<div id='flint-endpoint-mimeset-select-chooser' title='Select the output type that you wish to request'><select id='flint-endpoint-mimeset-select' name='output'>"
						+ selectChooser + "</select></div>";

				constructChooser = "<div id='flint-endpoint-mimeset-construct-chooser' title='Select the output type that you wish to request'><select id='flint-endpoint-mimeset-construct' name='output'>"
						+ constructChooser + "</select></div>";

				$('#flint-endpoint-output-formats').append(selectChooser);
				$('#flint-endpoint-output-formats').append(constructChooser);
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.disable = function() {
			$('#flint-endpoint-output-formats').css('visibility', 'hidden');
		};

		this.enable = function() {
			$('#flint-endpoint-output-formats').css('visibility', 'visible');
		};

		this.getMimeType = function() {
			try {
				var mimeType = "";
				if ($("#flint-endpoint-mimeset-select").is(":visible")) {
					mimeType = $("#flint-endpoint-mimeset-select").val();
				} else {
					mimeType = $("#flint-endpoint-mimeset-construct").val();
				}
				return mimeType;
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.setChangeAction = function(callback) {
		};
	}

	function FlintDatasetMimeTypePicker(config, editor) {

		this.setQueryType = function(queryType) {

			try {

				if (queryType === "SELECT") {

					$('#flint-dataset-mimeset-select-chooser').show();
					$('#flint-dataset-mimeset-construct-chooser').hide();

					if ($('#flint-coolbar').is(':visible')) {
						$('#flint-dataset-mimeset-select').attr('disabled', '');
						$('#flint-dataset-mimeset-construct').attr('disabled', 'disabled');
					} else {
						$('#flint-dataset-mimeset-select, #flint-dataset-mimeset-construct')
								.attr('disabled', 'disabled');
					}
				} else if (queryType === "CONSTRUCT" || queryType === "DESCRIBE") {
					$('#flint-dataset-mimeset-construct-chooser').show();
					$('#flint-dataset-mimeset-select-chooser').hide();

					if ($('#flint-coolbar').is(':visible')) {
						$('#flint-dataset-mimeset-construct').attr('disabled', '');
						$('#flint-dataset-mimeset-select').attr('disabled', 'disabled');
					} else {
						$('#flint-dataset-mimeset-select, #flint-dataset-mimeset-construct')
								.attr('disabled', 'disabled');
					}
				}
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.display = function(container) {
			try {
				var selectChooser = "";
				var constructChooser = "";

				// use output parameter for IE, otherwise accept header mimetype
				var type;
				if ($.browser.msie) {
					type = 'format';
				} else {
					type = 'type';
				}
				var i;
				for (i = 0; i < config.defaultEndpointParameters.selectFormats.length; i++) {
					selectChooser += "<option value='"
							+ config.defaultEndpointParameters.selectFormats[i][type] + "'>"
							+ config.defaultEndpointParameters.selectFormats[i].name
							+ "</option>";
				}

				for (i = 0; i < config.defaultEndpointParameters.constructFormats.length; i++) {
					constructChooser += "<option value='"
							+ config.defaultEndpointParameters.constructFormats[i][type]
							+ "'>"
							+ config.defaultEndpointParameters.constructFormats[i].name
							+ "</option>";
				}

				$('#' + container)
						.append(
								"<div id='flint-dataset-output-formats' title='Select the format in which you would like the results to be returned'><h2>Output</h2></div>");

				selectChooser = "<div id='flint-dataset-mimeset-select-chooser' title='Select the output type that you wish to request'><select id='flint-dataset-mimeset-select' name='output'>"
						+ selectChooser + "</select></div>";

				constructChooser = "<div id='flint-dataset-mimeset-construct-chooser' title='Select the output type that you wish to request'><select id='flint-dataset-mimeset-construct' name='output'>"
						+ constructChooser + "</select></div>";

				$('#flint-dataset-output-formats').append(selectChooser);
				$('#flint-dataset-output-formats').append(constructChooser);
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.disable = function() {
			$('#flint-dataset-output-formats').css('visibility', 'hidden');
		};

		this.enable = function() {
			$('#flint-dataset-output-formats').css('visibility', 'visible');
		};

		this.getMimeType = function() {
			try {
				var mimeType = "";
				if ($("#flint-dataset-mimeset-select").is(":visible")) {
					mimeType = $("#flint-dataset-mimeset-select").val();
				} else {
					mimeType = $("#flint-dataset-mimeset-construct").val();
				}
				return mimeType;
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.setChangeAction = function(callback) {
		};
	}

	function FlintCoolbar(config, editor) {

		var coolbarItems = [];

		try {
			coolbarItems.push(new FlintDatasetPicker(config, editor));
			coolbarItems.push(new FlintModePicker(config, editor, 'coolbar'));
			coolbarItems.push(new FlintDatasetQuerySubmitButton(editor));
			coolbarItems.push(new FlintDatasetMimeTypePicker(config, editor));
			this.getItems = function() {
				return coolbarItems;
			};
		} catch (e) {
			editor.getErrorBox().show(e);
		}

		this.hide = function() {
			$('#flint-coolbar').hide();
		};

		this.show = function() {
			$('#flint-coolbar').show();
		};

		this.display = function(container) {
			var listItems = "";
			var i;
			$('#' + container).append("<div id='flint-coolbar'></div>");
			for (i = 0; i < coolbarItems.length; i++) {
				listItems += coolbarItems[i].display('flint-coolbar');
			}
		};
	}

	function FlintEndpointBar(config, editor) {

		var barItems = [];

		try {
			barItems.push(new FlintEndpointEntry(config, editor));
			barItems.push(new FlintEndpointQuerySubmitButton(editor));
			barItems.push(new FlintEndpointDataInfoButton(editor));
			barItems.push(new FlintEndpointMimeTypePicker(config, editor));
			barItems.push(new FlintModePicker(config, editor, 'endpoint'));
			this.getItems = function() {
				return barItems;
			};
		} catch (e) {
			editor.getErrorBox().show(e);
		}

		this.hide = function() {
			$('#flint-endpoint-bar').hide();
		};

		this.show = function() {
			$('#flint-endpoint-bar').show();
		};

		this.display = function(container) {
			var listItems = "";
			var i;
			$('#' + container).append("<div id='flint-endpoint-bar'></div>");
			for (i = 0; i < barItems.length; i++) {
				listItems += barItems[i].display('flint-endpoint-bar');
			}
		};
	}

	function FlintSidebar(editor, config) {

		var activeDataItem;
		var activeTab = "SPARQL";
		var allKeywords = [];
		var visible = false; // Is sidebar visible?

		allKeywords = allKeywords.concat(editor.sparql1Keywords);

		function displaySparql() {
			$('#flint-sidebar-content').text("");
			var rowsize = 4;
			var commandFilterList = "<li>ALL</li>";
			var commandList = "<ul id='flint-command-table'>";
			var i;
			var j;
			for (i = 0; i < allKeywords.length; i += rowsize) {
				for (j = 0; (j < rowsize) && (i + j < allKeywords.length); ++j) {
					commandList += '<li><button type="button" disabled="true" id="flint-keyword-'
							+ allKeywords[i + j][0]
							+ '-button" title="'
							+ allKeywords[i + j][1]
							+ ' functions group" class="flint-keyword-button flint-keyword-group-'
							+ allKeywords[i + j][1]
							+ '">'
							+ allKeywords[i + j][0]
							+ '</button></li>';
					if (commandFilterList.indexOf(allKeywords[i + j][1]) === -1) {
						commandFilterList += "<li title='Filter view by "
								+ allKeywords[i + j][1] + " keywords'>" + allKeywords[i + j][1]
								+ "</li>";
					}
				}
			}
			commandList += "</ul>";

			commandFilterList = "<ul id='flint-sidebar-command-filter'>"
					+ commandFilterList + "</ul>";

			$('#flint-sidebar-content').append(
					commandFilterList + "<div id='flint-sidebar-commands'>" + commandList
							+ "</div>");

			$('#flint-sidebar-command-filter li').click(
					function() {
						var commandGroupStyle = "flint-keyword-group-" + $(this).text();
						if ($(this).text() === "ALL") {
							$('#flint-command-table button').show();
						} else {
							$('#flint-command-table button:not(.' + commandGroupStyle + ')')
									.hide();
							$('#flint-command-table .' + commandGroupStyle).show();
						}
					});

		}

		function calcPrefixes() {
			try {
				if (activeDataItem === null) {
					return;
				}
				if (config.namespaces !== null) {
					var listText = "";
					var prefixes = [];
					var j;
					for (j = 0; j < config.namespaces.length; j++) {
						var found = false;
						var uri;
						var prefix;
						var i;
						if (activeDataItem.properties != null) {
							uri = config.namespaces[j].uri;
							prefix = config.namespaces[j].prefix;
							for (i = 0; i < activeDataItem.properties.results.bindings.length; i++) {
								if (activeDataItem.properties.results.bindings[i].p.value
										.indexOf(uri) === 0) {
									prefixes.push(config.namespaces[j]);
									found = true;
									break;
								}
							}
						}
						if (!found && activeDataItem.classes != null) {
							uri = config.namespaces[j].uri;
							prefix = config.namespaces[j].prefix;
							for (i = 0; i < activeDataItem.classes.results.bindings.length; i++) {
								if (activeDataItem.classes.results.bindings[i].o.value
										.indexOf(uri) === 0) {
									prefixes.push(config.namespaces[j]);
									break;
								}
							}
						}
					}
					activeDataItem.prefixes = prefixes;
				}
			} catch (e) {
				editor.getErrorBox().show("Prefix calculation: " + e);
			}
		}

		this.getPrefixes = function() {

			if (activeDataItem == null) {
				return "";
			}

			var prefixText = "";
			if (activeDataItem.prefixes != null) {
				var i;
				for (i = 0; i < activeDataItem.prefixes.length; i++) {
					prefixText += "PREFIX " + activeDataItem.prefixes[i].prefix + ": <"
							+ activeDataItem.prefixes[i].uri + ">\n";
				}
			}
			return prefixText;
		};

		this.getPrefixCount = function() {

			if (activeDataItem == null) {
				return 0;
			}

			var count = 0;
			if (activeDataItem.prefixes != null) {
				count = activeDataItem.prefixes.length;
			}

			return count;
		};

		this.getActiveDataItem = function() {
			return activeDataItem;
		};

		this.clearActiveItem = function() {
			activeDataItem = null;
		};

		function displayPrefixes() {
			$('#flint-sidebar-content').text("");
			if (activeDataItem) {
				if (activeDataItem.prefixes != null) {
					try {
						var listText = "";
						var i;
						for (i = 0; i < activeDataItem.prefixes.length; i++) {
							listText += "<li class='flint-prefix' title='"
									+ activeDataItem.prefixes[i].name + "'>"
									+ activeDataItem.prefixes[i].prefix + "</li>";
						}
						listText = "<ul>" + listText + "</ul>";
						$('#flint-sidebar-content').append(listText);
						$('.flint-prefix').click(function(e) {
							editor.insert($(this).text());
							e.stopPropagation();
						});
					} catch (e) {
						editor.getErrorBox().show(e);
					}
				} else {
					$('#flint-sidebar-content').append("<p>No prefixes available</p>");
				}
			} else {
				$('#flint-sidebar-content').append(
						"<p>No prefixes have been retrieved</p>");
			}
		}

		function displaySamples() {
			$('#flint-sidebar-content').text("");
			if (activeDataItem) {
				if (activeDataItem.queries != null) {
					try {
						var sampleText = "";
						var i;
						for (i = 0; i < activeDataItem.queries.length; i++) {
							var query = activeDataItem.queries[i].query;
							query = query.replace(/</g, "&lt;");
							query = query.replace(/>/g, "&gt;");
							sampleText += "<div class='flint-sample' title=''Click to insert sample into editing pane'><h3>"
									+ activeDataItem.queries[i].name
									+ "</h3><p>"
									+ activeDataItem.queries[i].description
									+ "</p><pre class='flint-sample-content'>"
									+ query
									+ "</pre></div>";
						}
						sampleText = "<div id='flint-samples'>" + sampleText + "</div>";
						$('#flint-sidebar-content').append(sampleText);
						$('.flint-sample-content')
								.click(
										function(e) {
											var okay = true;
											var sample = $(this);
											if (editor.getCodeEditor().getValue() != "") {
												editor.getConfirmDialog().setCloseAction(function() {
													var result = editor.getConfirmDialog().getResult();
													if (result === "Okay") {
														var cm = editor.getCodeEditor();
														cm.setValue("");
														editor.insert(sample.text());
														// Format
														// query
														var maxlines = cm.lineCount();
														var ln;
														for (ln = 0; ln < maxlines; ++ln) {
															cm.indentLine(ln);
														}
													}
												});
												editor
														.getConfirmDialog()
														.show("Insert Sample Query",
																"<p>Are you sure you want to abandon the current text?</p>");
											}
											e.stopPropagation();
										});
					} catch (e) {
						editor.getErrorBox().show(e);
					}
				} else {
					$('#flint-sidebar-content').append("<p>No samples available</p>");
				}
			} else {
				$('#flint-sidebar-content').append("<p>Samples are not applicable</p>");
			}
		}

		function displayProperties() {
			$('#flint-sidebar-content').text("");
			if (activeDataItem) {
				if (activeDataItem.properties != null) {
					try {
						var listText = "";
						var i;
						for (i = 0; i < activeDataItem.properties.results.bindings.length; i++) {
							listText += "<li class='flint-property'>"
									+ activeDataItem.properties.results.bindings[i].p.value
									+ "</li>";
						}
						listText = "<ul>" + listText + "</ul>";
						$('#flint-sidebar-content').append(listText);
						$('.flint-property').click(function(e) {
							editor.insert("<" + $(this).text() + ">");
							e.stopPropagation();
						});
					} catch (e) {
						editor.getErrorBox().show(e);
					}
				} else {
					$('#flint-sidebar-content').append("<p>No properties available</p>");
				}
			} else {
				$('#flint-sidebar-content').append(
						"<p>No properties have been retrieved</p>");
			}
		}

		function displayClasses() {
			$('#flint-sidebar-content').text("");
			if (activeDataItem) {
				if (activeDataItem.classes != null) {
					try {
						var listText = "";
						var i;
						for (i = 0; i < activeDataItem.classes.results.bindings.length; i++) {
							listText += "<li class='flint-class'>"
									+ activeDataItem.classes.results.bindings[i].o.value
									+ "</li>";
						}
						listText = "<ul>" + listText + "</ul>";
						$('#flint-sidebar-content').append(listText);
						$('.flint-class').click(function(e) {
							editor.insert("<" + $(this).text() + ">");
							e.stopPropagation();
						});
					} catch (e) {
						editor.getErrorBox().show(e);
					}
				} else {
					$('#flint-sidebar-content').append("<p>No classes available</p>");
				}
			} else {
				$('#flint-sidebar-content').append(
						"<p>No classes have been retrieved</p>");
			}
		}

		function showTab(tabName, id) {
			activeTab = tabName;
			$('#flint-sidebar-options li').removeAttr("class");
			$('#' + id).attr("class", "flint-sidebar-selected");
			if (tabName == "Properties") {
				displayProperties();
			} else if (tabName === "Classes") {
				displayClasses();
			} else if (tabName === "Prefixes") {
				displayPrefixes();
			} else if (tabName === "Samples") {
				displaySamples();
			} else {
				$('#flint-sidebar-sparql').attr("class", "flint-sidebar-selected");
				displaySparql();
			}
		}

		this.showActiveTab = function() {
			showTab(activeTab);
		};

		// Is the sidebar visible?
		this.visible = function() {
			return visible;
		};

		this.display = function(container) {
			var listItems = "";
			$('#' + container)
					.append(
							"<div id='flint-sidebar'>"
									+ "<ul id='flint-sidebar-options'>"
									+ "<li id='flint-sidebar-sparql' title='View a list of SPARQL commands that can be inserted into the query'>SPARQL</li>"
									+ "<li id='flint-sidebar-properties' title='View a list of properties for the current dataset'>Properties</li>"
									+ "<li id='flint-sidebar-classes' title='View a list of classes for the current dataset'>Classes</li>"
									+ "<li id='flint-sidebar-prefixes' title='View a list of known prefixes for the current dataset'>Prefixes</li>"
									+ "<li id='flint-sidebar-samples' title='View sample queries for the current dataset'>Samples</li>"
									+ "</ul><div id='flint-sidebar-content'></div></div>"
									+ "<div id='flint-sidebar-grabber'><span id='flint-sidebar-grabber-button' title='Click to expand/shrink the tools pane'></span></div>");
			$('#flint-sidebar-grabber').click(function() {
				try {
					var editorWidth = $('#flint-editor').width();
					if (visible) {
						$('#flint-sidebar').css("width", "30px");
						$('#flint-sidebar-content').css("overflow", "hidden");
						$('#flint-samples').css("white-space", "nowrap");
						if (config.interface.toolbar) {
							if (editor.getToolbar) {
								editor.getToolbar().setEnabled("Show Tools", true);
								editor.getToolbar().setEnabled("Hide Tools", false);
							}
							if (editor.getMenu) {
								editor.getMenu().setEnabled("Show Tools", true);
								editor.getMenu().setEnabled("Hide Tools", false);
							}
						}
						visible = false;
					} else {
						$('#flint-sidebar').css("width", editorWidth / 2 + "px");
						$('#flint-sidebar-content').css("overflow", "auto");
						$('#flint-samples').css("white-space", "wrap");
						if (config.interface.toolbar) {
							if (editor.getToolbar) {
								editor.getToolbar().setEnabled("Show Tools", false);
								editor.getToolbar().setEnabled("Hide Tools", true);
							}
							if (editor.getMenu) {
								editor.getMenu().setEnabled("Show Tools", false);
								editor.getMenu().setEnabled("Hide Tools", true);
							}
						}
						visible = true;
					}
					// Force other UI components to resize
					$(window).resize();
				} catch (e) {
					editor.getErrorBox().show(e);
				}
			});
			$('#flint-sidebar-sparql').click(function(e) {
				showTab("SPARQL", $(this).attr("id"));
				e.stopPropagation();
			});
			$('#flint-sidebar-properties').click(function(e) {
				showTab("Properties", $(this).attr("id"));
				e.stopPropagation();
			});
			$('#flint-sidebar-classes').click(function(e) {
				showTab("Classes", $(this).attr("id"));
				e.stopPropagation();
			});
			$('#flint-sidebar-prefixes').click(function(e) {
				showTab("Prefixes", $(this).attr("id"));
				e.stopPropagation();
			});
			$('#flint-sidebar-samples').click(function(e) {
				showTab("Samples", $(this).attr("id"));
				e.stopPropagation();
			});
		};

		// Expects an array of keywords that are valid and a callback function
		// for when the keyword is clicked
		this.updateKeywords = function(possibles, buttonCallback) {

			var keywordLength = allKeywords.length;
			var mode = editor.getCodeEditor().getOption("mode");
			allKeywords = [];
			if (mode === "sparql11query") {
				allKeywords = editor.sparql11Query;
			} else if (mode === "sparql11update") {
				allKeywords = editor.sparql11Update;
			} else {
				allKeywords = editor.sparql1Keywords;
			}
			// If keywords have changed redisplay;
			if (keywordLength !== allKeywords.length) {
				displaySparql();
			}

			var i;
			var j;
			for (i = 0; i < allKeywords.length; ++i) {
				var enabled = false;
				var keyword = allKeywords[i][0];
				for (j = 0; j < possibles.length && !enabled; ++j) {
					if (keyword == possibles[j]) {
						enabled = true;
						break;
					}
				}
				var button = $('#flint-keyword-' + keyword + '-button');
				if (enabled) {
					button.attr("disabled", false);
					button.unbind("click");
					button.click(buttonCallback(keyword));
				} else {
					button.attr("disabled", true);
				}
			}
		};

		this.updateSamples = function(datasetItem) {
			activeDataItem = datasetItem;
			if (activeTab === "Samples") {
				displaySamples();
			}
		};

		this.updateProperties = function(datasetItem) {
			try {
				// Don't get properties for update endpoints
				if (datasetItem.modes) {
					var i;
					for (i = 0; i < datasetItem.modes.length; i++) {
						if (datasetItem.modes[i] === "sparql11update")
							return;
					}
				}

				if (datasetItem.properties == null) {
					activeDataItem = datasetItem;
					this.showActiveTab();
					var paramsData = {};
					paramsData[config.defaultEndpointParameters.queryParameters.query] = "SELECT DISTINCT ?p WHERE {?s ?p ?o} ORDER BY ?p LIMIT 1000";
					$
							.ajax({
								url : datasetItem.uri,
								data : paramsData,
								type : 'post',
								headers : {
									"Accept" : "application/sparql-results+json"
								},
								dataType : 'json',
								error : function(XMLHttpRequest, textStatus, errorThrown) {
									editor.getErrorBox().show(
											"Properties cannot be retrieved. HTTP Status: "
													+ XMLHttpRequest.status + ", " + errorThrown);
								},
								success : function(data) {
									datasetItem.properties = data;
									if (activeTab === "Properties") {
										displayProperties();
									}
									calcPrefixes();
									if (activeTab === "Prefixes") {
										displayPrefixes();
									}
									if (datasetItem.properties.results.bindings.length == 1000) {
										window
												.alert("The maximum number of properties has been reached - 1000");
									}

								}
							});
				} else {
					activeDataItem = datasetItem;
					if (activeTab === "Properties") {
						displayProperties();
					}
					calcPrefixes();
					if (activeTab === "Prefixes") {
						displayPrefixes();
					}
				}
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};

		this.updateClasses = function(datasetItem) {
			try {
				// Don't get properties for update endpoints
				if (datasetItem.modes) {
					var i;
					for (i = 0; i < datasetItem.modes.length; i++) {
						if (datasetItem.modes[i] === "sparql11update")
							return;
					}
				}

				if (datasetItem.classes == null) {
					activeDataItem = datasetItem;
					this.showActiveTab();
					var paramsData = {};
					paramsData[config.defaultEndpointParameters.queryParameters.query] = "SELECT DISTINCT ?o WHERE {?s a ?o} ORDER BY ?o LIMIT 1000";
					$
							.ajax({
								url : datasetItem.uri,
								data : paramsData,
								type : 'post',
								headers : {
									"Accept" : "application/sparql-results+json"
								},
								dataType : 'json',
								error : function(XMLHttpRequest, textStatus, errorThrown) {
									editor.getErrorBox().show(
											"Classes cannot be retrieved. HTTP Status: "
													+ XMLHttpRequest.status + ", " + errorThrown);
								},
								success : function(data) {
									datasetItem.classes = data;
									if (activeTab === "Classes") {
										displayClasses();
									}
									calcPrefixes();
									if (activeTab === "Prefixes") {
										displayPrefixes();
									}
									if (datasetItem.classes.results.bindings.length == 1000) {
										window
												.alert("The maximum number of classes has been reached - 1000");
									}
								}
							});
				} else {
					activeDataItem = datasetItem;
					if (activeTab === "Classes") {
						displayClasses();
					}
					calcPrefixes();
					if (activeTab === "Prefixes") {
						displayPrefixes();
					}
				}
			} catch (e) {
				editor.getErrorBox().show(e);
			}
		};
	}

	// This next section is the code for the actual editing window

	function FlintCodeEditor(flint, editorMode) {

		var initialQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\nSELECT * WHERE {\n   ?s ?p ?o\n}\nLIMIT 10";
		var cm;
		var spaceForTabsCount = 0;
		var tabOffset = 0;
		var clearError = function() {
		};
		var markerHandle = null;
		var tabItems = [];
		var activeTab = 0;
		var tabsCount = 0;
		var codeEditor = this;
		var queryType = "";

		this.getTitle = function() {
			return "";
		};

		// ----------------------------------------------------------------
		// Autocompletion code, based on the example for javascript

		function stopEvent() {
			if (this.preventDefault) {
				this.preventDefault();
				this.stopPropagation();
			} else {
				this.returnValue = false;
				this.cancelBubble = true;
			}
		}

		function addStop(event) {
			if (!event.stop) {
				event.stop = stopEvent;
			}
			return event;
		}

		function connect(node, type, handler) {

			function wrapHandler(event) {
				handler(addStop(event || window.event));
			}

			if (typeof node.addEventListener == "function") {
				node.addEventListener(type, wrapHandler, false);
			} else {
				node.attachEvent("on" + type, wrapHandler);
			}
		}

		function forEach(arr, f) {
			var i;
			var e;
			for (i = 0, e = arr.length; i < e; ++i) {
				f(arr[i]);
			}
		}

		function memberChk(el, arr) {
			var i;
			var e;
			for (i = 0, e = arr.length; i < e; ++i) {
				if (arr[i] == el) {
					return (true);
				}
			}
			return false;
		}

		// Extract context info needed for autocompletion / keyword buttons
		// based on cursor position
		function getPossiblesAtCursor() {
			// We want a single cursor position.
			if (cm.somethingSelected()) {
				return;
			}
			// Find the token at the cursor
			var cur = cm.getCursor(false);
			var cur1 = {
				line : cur.line,
				ch : cur.ch
			};

			// Before cursor
			var charBefore = cm.getRange({
				line : cur.line,
				ch : cur.ch - 1
			}, {
				line : cur.line,
				ch : cur.ch
			});

			// Cursor position on the far left (ch=0) is problematic
			// - if we ask CodeMirror for token at this position, we don't
			// get back the token at the beginning of the line
			// - hence use adjusted position cur1 to recover this token.
			if (cur1.ch == 0 && cm.lineInfo(cur1.line).text.length > 0) {
				cur1.ch = 1;
			}
			var token = cm.getTokenAt(cur1);
			var charAfter;
			var possibles = null;
			var start = token.string.toLowerCase();
			var insertPos = null;
			var insertEnd = false;
			var insertStart = false;

			// if the token is whitespace, use empty string for matching
			// and set insertPos, so that selection will be inserted into
			// into space, rather than replacing it.
			if (token.className == "sp-ws") {
				start = "";
				// charAfter is char after cursor
				charAfter = cm.getRange({
					line : cur.line,
					ch : cur.ch
				}, {
					line : cur.line,
					ch : cur.ch + 1
				});
				insertPos = cur;
			} else {
				// charAfter is char after end of token
				charAfter = cm.getRange({
					line : cur.line,
					ch : token.end
				}, {
					line : cur.line,
					ch : token.end + 1
				});
				if (token.className != "sp-invalid"
						&& token.className != "sp-prefixed"
						&& (token.string != "<" || !memberChk("IRI_REF",
								token.state.possibleCurrent))
				// OK when "<" is start of URI
				) {
					if (token.end == cur.ch && token.end != 0) {
						insertEnd = true;
						start = "";
						insertPos = cur;
					} else if (token.start == cur.ch) {
						insertStart = true;
						start = "";
						insertPos = cur;
					}
				}
			}

			if (token.className === "sp-comment") {
				possibles = [];
			} else {
				if (cur1.ch > 0 && !insertEnd) {
					possibles = token.state.possibleCurrent;
				} else {
					possibles = token.state.possibleNext;
				}
			}

			return {
				"token" : token, // codemirror token object
				"possibles" : possibles, // array of possibles terminals from
				// grammar
				"insertPos" : insertPos, // Position in line to insert text,
				// or null if replacing existing
				// text
				"insertStart" : insertStart, // true if position of insert
				// adjacent to start of a non-ws
				// token
				"insertEnd" : insertEnd, // true if ... ... end of a ...
				"charAfter" : charAfter, // char found straight after cursor
				"cur" : cur, // codemirror {line,ch} object giving pos of
				// cursor
				"start" : start
			// Start of token for autocompletion
			};
		}

		var keywords = /^(GROUP_CONCAT|DATATYPE|BASE|PREFIX|SELECT|CONSTRUCT|DESCRIBE|ASK|FROM|NAMED|ORDER|BY|LIMIT|ASC|DESC|OFFSET|DISTINCT|REDUCED|WHERE|GRAPH|OPTIONAL|UNION|FILTER|GROUP|HAVING|AS|VALUES|LOAD|CLEAR|DROP|CREATE|MOVE|COPY|SILENT|INSERT|DELETE|DATA|WITH|TO|USING|NAMED|MINUS|BIND|LANGMATCHES|LANG|BOUND|SAMETERM|ISIRI|ISURI|ISBLANK|ISLITERAL|REGEX|TRUE|FALSE|UNDEF|ADD|DEFAULT|ALL|SERVICE|INTO|IN|NOT|IRI|URI|BNODE|RAND|ABS|CEIL|FLOOR|ROUND|CONCAT|STRLEN|UCASE|LCASE|ENCODE_FOR_URI|CONTAINS|STRSTARTS|STRENDS|STRBEFORE|STRAFTER|YEAR|MONTH|DAY|HOURS|MINUTES|SECONDS|TIMEZONE|TZ|NOW|UUID|STRUUID|MD5|SHA1|SHA256|SHA384|SHA512|COALESCE|IF|STRLANG|STRDT|ISNUMERIC|SUBSTR|REPLACE|EXISTS|COUNT|SUM|MIN|MAX|AVG|SAMPLE|SEPARATOR|STR)$/i;
		var punct = /^(\*|\.|\{|\}|,|\(|\)|;|\[|\]|\|\||&&|=|!=|!|<=|>=|<|>|\+|-|\/|\^\^|\?|\||\^)$/;
		function getCompletions(token, start, possibles) {

			var found = [];

			var state = token.state;
			var stack = state.stack;
			var top = stack.length - 1;
			var topSymbol = stack[top];

			// Skip optional clutter
			while (/^(\*|\?).*/.test(topSymbol) && top > 0) {
				topSymbol = stack[--top];
			}

			var lastProperty = token.state.lastProperty;
			// Is a class expected in this position?
			// If the preceding property was rdf:type and an object is expected,
			// then a class is expected.
			var isClassPos = false;
			if (lastProperty
					.match(/^a|rdf:type|<http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type>$/)
					&& ((start == "" && (topSymbol == "object"
							|| topSymbol == "objectList" || topSymbol == "objectListPath")) || (start != "" && topSymbol == "}"))) {
				isClassPos = true;
			}

			// test the case of the 1st non-space char
			var startIsLowerCase = /^ *[a-z]/.test(token.string);

			// Where case is flexible
			function maybeAdd(str) {
				if (str.toUpperCase().indexOf(start.toUpperCase()) === 0) {
					if (startIsLowerCase) {
						found.push(str.toLowerCase());
					} else {
						found.push(str.toUpperCase());
					}
				}
			}

			// Where case is not flexible
			function maybeAddCS(str) {
				if (str.toUpperCase().indexOf(start.toUpperCase()) === 0) {
					found.push(str);
				}
			}

			// Add items from the fetched sets of properties / classes
			// set is "properties" or "classes"
			// varName is "p" or "o"
			function addFromCollectedURIs(set, varName) {
				if (/:/.test(start)) {
					// Prefix has been entered - give items matching prefix
					var activeDataItem = editor.getSidebar().getActiveDataItem();
					if (activeDataItem) {
						for ( var k = 0; k < activeDataItem.prefixes.length; k++) {
							var ns = activeDataItem.prefixes[k].uri;
							for ( var j = 0; j < activeDataItem[set].results.bindings.length; j++) {
								var fragments = activeDataItem[set].results.bindings[j][varName].value
										.match(/(^\S*[#\/])([^#\/]*$)/);
								if (fragments.length == 3 && fragments[1] == ns)
									maybeAddCS(activeDataItem.prefixes[k].prefix + ":"
											+ fragments[2]);
							}
						}
					}
				} else if (/^</.test(start)) {
					// Looks like a URI - add matching URIs
					var activeDataItem = editor.getSidebar().getActiveDataItem();
					if (activeDataItem) {
						for ( var j = 0; j < activeDataItem[set].results.bindings.length; j++)
							maybeAddCS("<"
									+ activeDataItem[set].results.bindings[j][varName].value
									+ ">");
					}
				}
			}

			function gatherCompletions() {
				var i;
				var j;
				var activeDataItem;
				if (isClassPos)
					addFromCollectedURIs("classes", "o");
				for (i = 0; i < possibles.length; ++i) {
					if (possibles[i] == "VAR1" && state.allowVars) {
						maybeAddCS("?");
					} else if (keywords.exec(possibles[i])) {
						// keywords - the strings stand for themselves
						maybeAdd(possibles[i]);
					} else if (punct.exec(possibles[i])) {
						// punctuation - the strings stand for themselves
						maybeAddCS(possibles[i]);
					} else if (possibles[i] == "STRING_LITERAL1") {
						maybeAddCS('"');
						maybeAddCS("'");
					} else if (possibles[i] == "IRI_REF" && !/^</.test(start)) {
						maybeAddCS("<");
					} else if (possibles[i] == "BLANK_NODE_LABEL" && state.allowBnodes) {
						maybeAddCS("_:");
					} else if (possibles[i] == "a") {
						// Property expected here - fetch possibilities
						maybeAddCS("a");
						addFromCollectedURIs("properties", "p");
					} else if (possibles[i] == "PNAME_LN" && !/:$/.test(start)) {
						// prefixed names - offer prefixes
						activeDataItem = editor.getSidebar().getActiveDataItem();
						if (activeDataItem !== undefined
								&& activeDataItem.prefixes != undefined
								&& activeDataItem.prefixes.length) {
							for (j = 0; j < activeDataItem.prefixes.length; j++) {
								maybeAddCS(activeDataItem.prefixes[j].prefix + ":");
							}
						}
					}
				}
			}

			gatherCompletions();
			return found;
		}

		function insertOrReplace(str, tkposs) {
			if ((tkposs.insertStart || tkposs.charAfter !== " ")
					&& /^[A-Za-z\*]*$/.exec(str)) {
				str = str + " ";
			}
			if (tkposs.insertEnd) {
				str = " " + str;
			}
			if (tkposs.insertPos) {
				// Insert between spaces
				cm.replaceRange(str, tkposs.insertPos);
			} else {
				// Replace existing token
				cm.replaceRange(str, {
					line : tkposs.cur.line,
					ch : tkposs.token.start
				}, {
					line : tkposs.cur.line,
					ch : tkposs.token.end
				});
			}
		}

		function startComplete() {

			// We want a single cursor position.
			if (cm.somethingSelected()) {
				return;
			}

			var tkposs = getPossiblesAtCursor();
			var stack = tkposs.token.state.stack;

			var completions = getCompletions(tkposs.token, tkposs.start,
					tkposs.possibles);

			if (!completions.length) {
				return;
			}

			// When there is only one completion, use it directly.
			if (completions.length === 1) {
				insertOrReplace(completions[0], tkposs);
				return true;
			}

			// Build the select widget
			var complete = document.createElement("div");
			complete.className = "completions";
			var sel = complete.appendChild(document.createElement("select"));
			sel.multiple = true;
			var i;
			for (i = 0; i < completions.length; ++i) {
				var opt = sel.appendChild(document.createElement("option"));
				opt.appendChild(document.createTextNode(completions[i]));
			}
			sel.firstChild.selected = true;
			sel.size = Math.min(10, completions.length);
			var pos = cm.cursorCoords();

			complete.style.position = "absolute";
			complete.style.left = pos.x + "px";
			complete.style.top = pos.yBot + "px";

			document.body.appendChild(complete);

			// Hack to hide the scrollbar.
			if (completions.length <= 10) {
				complete.style.width = (sel.clientWidth - 1) + "px";
			}

			var done = false;
			function close() {
				if (done) {
					return;
				}
				done = true;
				complete.parentNode.removeChild(complete);
			}
			function pick() {
				insertOrReplace(sel.options[sel.selectedIndex].value, tkposs);
				close();
				setTimeout(function() {
					cm.focus();
				}, 50);
			}
			connect(sel, "blur", close);
			connect(sel, "keydown", function(event) {
				var code = event.keyCode;
				// Enter and space
				if (code === 13 || code === 32) {
					event.stop();
					pick();
				}
				// Escape
				else if (code === 27) {
					event.stop();
					close();
					cm.focus();
				} else if (code !== 38 && code !== 40) {
					close();
					cm.focus();
					setTimeout(startComplete, 50);
				}
			});
			connect(sel, "dblclick", pick);

			sel.focus();
			// Opera sometimes ignores focusing a freshly created node
			if (window.opera) {
				setTimeout(function() {
					if (!done) {
						sel.focus();
					}
				}, 100);
			}
			return true;
		}

		function autocompleteKeyEventHandler(i, e) {
			// Hook into ctrl-space
			if (e.keyCode == 32 && (e.ctrlKey || e.metaKey) && !e.altKey) {
				e.stop();
				return startComplete();
			}
		}

		function cmUpdate() {

			if (clearError !== null) {
				clearError();
				clearError = null;
			}

			if (markerHandle !== null) {
				cm.clearMarker(markerHandle);
			}
			var state;
			var l;
			for (l = 0; l < cm.lineCount(); ++l) {
				state = cm.getTokenAt({
					line : l,
					ch : cm.getLine(l).length
				}).state;
				if (state.OK === false) {
					markerHandle = cm
							.setMarker(l,
									"<span style=\"color: #f00 ; font-size: large;\">&rarr;</span> %N%");
					clearError = cm.markText({
						line : l,
						ch : state.errorStartPos
					}, {
						line : l,
						ch : state.errorEndPos
					}, "sp-error");
					break;
				}
			}

			if (state.complete) {
				// Coolbar submit item
				flint.getCoolbar().getItems()[2].enable();
				// Endpoint bar submit item
				flint.getEndpointBar().getItems()[1].enable();
				flint.getStatusArea().setQueryValid(true);
			} else {
				flint.getCoolbar().getItems()[2].disable();
				flint.getEndpointBar().getItems()[1].disable();
				flint.getStatusArea().setQueryValid(false);
			}

			// Dataset bar MIME type selection
			flint.getCoolbar().getItems()[3].setQueryType(state.queryType);
			// Endpoint bar MIME type selection
			flint.getEndpointBar().getItems()[3].setQueryType(state.queryType);
			flint.getStatusArea().updateStatus();
			if (state.queryType) {
				queryType = state.queryType.toUpperCase();
			} else {
				queryType = "";
			}
		}

		// Enable/disable the keyword buttons depending on the possibilities at
		// cursor position
		function updateKeywordTable() {

			var tkposs = getPossiblesAtCursor();

			function getButtonFn(str) {
				return function(e) {
					insertOrReplace(str, tkposs);
					cm.focus();
					e.stopPropagation();
				};
			}

			if (tkposs != undefined) {
				// Update keywords in the sidebar
				flint.getSidebar().updateKeywords(tkposs.possibles, getButtonFn);
			}
		}

		function cmCursor() {

			updateKeywordTable();

			if (cm.somethingSelected() != "") {
				if (flint.getToolbar)
					flint.getToolbar().setEnabled("Cut", true);
				if (flint.getMenu)
					flint.getMenu().setEnabled("Cut", true);
			} else {
				if (flint.getToolbar)
					flint.getToolbar().setEnabled("Cut", false);
				if (flint.getMenu)
					flint.getMenu().setEnabled("Cut", false);
			}
			
			if (cm.historySize().undo > 0) {
				if (flint.getToolbar)
					flint.getToolbar().setEnabled("Undo", true);
				if (flint.getMenu)
					flint.getMenu().setEnabled("Undo", true);
			} else {
				if (flint.getToolbar)
					flint.getToolbar().setEnabled("Undo", false);
				if (flint.getMenu)
					flint.getMenu().setEnabled("Undo", false);
			}
			
			if (cm.historySize().redo > 0) {
				if (flint.getToolbar)
					flint.getToolbar().setEnabled("Redo", true);
				if (flint.getMenu)
					flint.getMenu().setEnabled("Redo", true);
			} else {
				if (flint.getToolbar)
					flint.getToolbar().setEnabled("Redo", false);
				if (flint.getMenu)
					flint.getMenu().setEnabled("Redo", false);
			}

			flint.getStatusArea().setLine(cm.getCursor().line);
			flint.getStatusArea().setPosition(cm.getCursor().ch);
			flint.getStatusArea().updateStatus();
		}

		this.setCodeMode = function(editorMode) {
			cm.setOption("mode", editorMode);
			updateKeywordTable();
		};

		this.getCodeMirror = function() {
			return cm;
		};

		this.getQueryType = function() {
			return queryType;
		};

		// Calculate number of tabs actually displayed
		function getFunctioningTabs() {
			var functioning = 0;
			var i;
			for (i = 0; i < tabItems.length; i++) {
				if (tabItems[i] != null) {
					functioning++;
				}
			}
			return functioning;
		}

		function storeCurrentTab() {
			var currentTabContent = cm.getValue();
			tabItems[activeTab].setText(currentTabContent);
			tabItems[activeTab].setCursor(cm.getCursor().line, cm.getCursor().ch);
		}

		function displayCurrentTab() {
			var currentTabItem = tabItems[activeTab];
			cm.setValue(currentTabItem.getText());
			cm.setCursor(currentTabItem.getLine(), currentTabItem.getCh());
			$('#flint-editor-tabs li').attr("class", "flint-editor-tab");
			$('#flint-tab-' + (activeTab + 1)).attr("class",
					"flint-editor-tab-selected");
			cm.focus();
		}

		function closeTab(tabIndex) {
			$('#flint-tab-' + tabIndex).remove();
			// If we're closing the current tab we need to display another
			tabItems[tabIndex - 1] = null;
			var foundTab = false;
			var i;
			for (i = 0; i < tabItems.length; i++) {
				if (tabItems[i] != null) {
					activeTab = i;
					foundTab = true;
					break;
				}
			}
			// Always ensure we have at least one query tab
			if (!foundTab) {
				codeEditor.addTab("");
			} else {
				if (tabOffset > 0) {
					tabOffset--;
					$('#flint-editor-tabs li:eq(' + tabOffset + ')').show();
				}
				displayCurrentTab();
			}
			checkTabScroll();
		}

		function checkTabScroll() {
			var functioningTabs = getFunctioningTabs();
			var containerWidth = $('#flint-editor-tabs').width();
			if (containerWidth < (functioningTabs * 102)) {
				if (tabOffset > 0) {
					$('#flint-scroll-tabs-left').css('opacity', 1);
				} else {
					$('#flint-scroll-tabs-left').css('opacity', 0.5);
				}
				if (tabOffset < (functioningTabs - spaceForTabsCount)) {
					$('#flint-scroll-tabs-right').css('opacity', 1);
				} else {
					$('#flint-scroll-tabs-right').css('opacity', 0.5);
				}
			} else {
				$('#flint-scroll-tabs-left').css('opacity', 0.5);
				$('#flint-scroll-tabs-right').css('opacity', 0.5);
			}
		}

		this.addTab = function(content) {
			if ($('#flint-editor-tabs li').length > 0) {
				storeCurrentTab();
			}
			if (content == null) {
				content = "";
			}
			tabItems.push(new FlintEditItem(content));
			tabsCount++;
			$('#flint-editor-tabs')
					.append(
							"<li class='flint-editor-tab' id='flint-tab-"
									+ tabsCount
									+ "'>Query "
									+ tabsCount
									+ "<span class='flint-tab-close' title='Close this tab'></span></li>");
			activeTab = tabsCount - 1;
			displayCurrentTab();
			$('#flint-editor-tabs li:last-child').click(function(tabIndex) {
				return function() {
					storeCurrentTab();
					activeTab = tabIndex - 1;
					displayCurrentTab();
				};
			}(tabsCount));
			$('#flint-editor-tabs li:last-child .flint-tab-close').click(
					function(tabIndex) {
						return function() {
							closeTab(tabIndex);
						};
					}(tabsCount));
			checkTabScroll();
		};

		// If window has been resize recalculate display of some components
		this.resize = function() {
			var containerWidth = $('#flint-editor-tabs-container').width();
			$('#flint-editor-tabs').css('max-width', containerWidth - 70);
			spaceForTabsCount = Math.floor((containerWidth - 70) / 102);
			tabOffset = 0;
			$('#flint-editor-tabs li').show();
			checkTabScroll();
		};

		this.display = function(container) {
			$('#' + container)
					.append(
							"<div id='flint-editor-container'><div id='flint-editor-tabs-container'><div id='flint-scroll-tabs-left'><img src='"
									+ flint.getImagesPath()
									+ "/Previous.png'/></div>"
									+ "<ul id='flint-editor-tabs'></ul>"
									+ "<div id='flint-scroll-tabs-right'><img src='"
									+ flint.getImagesPath()
									+ "/Next.png'/></div></div>"
									+ "<textarea id='flint-code' name='query' cols='100' rows='1'>"
									+ initialQuery + "</textarea></div>");

			cm = CodeMirror.fromTextArea(document.getElementById("flint-code"), {
				mode : editorMode,
				lineNumbers : true,
				indentUnit : 3,
				tabMode : "indent",
				matchBrackets : true,
				onHighlightComplete : cmUpdate,
				onCursorActivity : cmCursor,
				onKeyEvent : autocompleteKeyEventHandler
			});

			this.addTab(initialQuery);
			var containerWidth = $('#flint-editor-tabs-container').width();
			$('#flint-editor-tabs').css('max-width', containerWidth - 70);

			$('#flint-scroll-tabs-left').click(function() {
				if (tabOffset > 0 && $('#flint-scroll-tabs-left').css('opacity') == 1) {
					tabOffset--;
					$('#flint-editor-tabs li:eq(' + tabOffset + ')').show();
					checkTabScroll();
				}
			});

			$('#flint-scroll-tabs-right').click(
					function() {
						var functioningTabs = getFunctioningTabs();
						if (tabOffset < (functioningTabs - spaceForTabsCount)
								&& $('#flint-scroll-tabs-right').css('opacity') == 1) {
							tabOffset++;
							$('#flint-editor-tabs li:lt(' + tabOffset + ')').hide();
							checkTabScroll();
						}
					});

		};

		// Stores all the information for an editor tab item
		function FlintEditItem(text) {

			var content = text;
			var cursorLine = 0;
			var cursorCh = 0;

			this.setText = function(text) {
				content = text;
			};

			this.getText = function() {
				return content;
			};

			this.getLine = function() {
				return cursorLine;
			};

			this.getCh = function() {
				return cursorCh;
			};

			this.setCursor = function(line, ch) {
				cursorLine = line;
				cursorCh = ch;
			};
		}

	}

	function Flint(container, imagesPath, config) {

		var flint = this, editorId = "flint-editor", showResultsInSitu = true, cm, createToolbar, createMenu;

		// Add errorbox - this reuses the standard Flint dialog
		var errorBox = new FlintError(flint);
		this.getErrorBox = function() {
			return errorBox;
		};

		this.windowClosing = false;

		// Don't display dialogs when navigating away from page
		$(window).bind('beforeunload', function(event) {
			editor.windowClosing = true;
		});

		// Keywords can be grouped using a string for the second array item
		this.sparql1Keywords = [ [ "BASE", "" ], [ "PREFIX", "" ],
				[ "SELECT", "" ], [ "ASK", "" ], [ "CONSTRUCT", "" ],
				[ "DESCRIBE", "" ], [ "DISTINCT", "MODIFIER" ], [ "REDUCED", "" ],
				[ "FROM", "" ], [ "NAMED", "" ], [ "WHERE", "" ], [ "GRAPH", "" ],
				[ "UNION", "" ], [ "FILTER", "" ], [ "OPTIONAL", "" ],
				[ "ORDER", "MODIFIER" ], [ "LIMIT", "MODIFIER" ],
				[ "OFFSET", "MODIFIER" ], [ "BY", "MODIFIER" ], [ "ASC", "" ],
				[ "DESC", "" ], [ "STR", "STRING" ], [ "LANG", "" ],
				[ "LANGMATCHES", "STRING" ], [ "DATATYPE", "" ], [ "BOUND", "" ],
				[ "SAMETERM", "" ], [ "ISIRI", "TERM" ], [ "ISURI", "TERM" ],
				[ "ISBLANK", "TERM" ], [ "ISLITERAL", "TERM" ], [ "REGEX", "STRING" ] ];

		this.sparql11Query = [ [ "BASE", "" ], [ "PREFIX", "" ], [ "SELECT", "" ],
				[ "ASK", "" ], [ "CONSTRUCT", "" ], [ "DESCRIBE", "" ],
				[ "DISTINCT", "MODIFIER" ], [ "REDUCED", "" ], [ "FROM", "" ],
				[ "NAMED", "" ], [ "WHERE", "" ], [ "GRAPH", "" ], [ "UNION", "" ],
				[ "FILTER", "" ], [ "OPTIONAL", "" ], [ "ORDER", "MODIFIER" ],
				[ "LIMIT", "MODIFIER" ], [ "OFFSET", "MODIFIER" ],
				[ "BY", "MODIFIER" ], [ "ASC", "" ], [ "DESC", "" ],
				[ "STR", "STRING" ], [ "LANG", "" ], [ "LANGMATCHES", "STRING" ],
				[ "DATATYPE", "" ], [ "BOUND", "" ], [ "SAMETERM", "" ],
				[ "ISIRI", "TERM" ], [ "ISURI", "TERM" ], [ "ISBLANK", "TERM" ],
				[ "ISLITERAL", "TERM" ], [ "REGEX", "STRING" ], [ "HAVING", "" ],
				[ "GROUP", "" ], [ "VALUES", "" ], [ "IF", "" ], [ "COALESCE", "" ],
				[ "EXISTS", "" ], [ "NOT", "" ], [ "ISNUMERIC", "TERM" ],
				[ "IRI", "TERM" ], [ "BNODE", "TERM" ], [ "STRDT", "TERM" ],
				[ "STRLANG", "TERM" ], [ "UUID", "TERM" ], [ "STRUUID", "TERM" ],
				[ "STRLEN", "STRING" ], [ "SUBSTR", "STRING" ], [ "LCASE", "STRING" ],
				[ "UCASE", "STRING" ], [ "STRSTARTS", "STRING" ],
				[ "STRENDS", "STRING" ], [ "CONTAINS", "STRING" ],
				[ "STRBEFORE", "STRING" ], [ "STRAFTER", "STRING" ],
				[ "ENCODE_FOR_URI", "STRING" ], [ "CONCAT", "STRING" ],
				[ "REPLACE", "STRING" ], [ "NOW", "DATE" ], [ "YEAR", "DATE" ],
				[ "MONTH", "DATE" ], [ "DAY", "DATE" ], [ "HOURS", "DATE" ],
				[ "MINUTES", "DATE" ], [ "SECONDS", "DATE" ], [ "TIMEZONE", "DATE" ],
				[ "TZ", "DATE" ], [ "MD5", "HASH" ], [ "SHA1", "HASH" ],
				[ "SHA256", "HASH" ], [ "SHA384", "HASH" ], [ "SHA512", "HASH" ],
				[ "ABS", "NUMERIC" ], [ "ROUND", "NUMERIC" ], [ "CEIL", "NUMERIC" ],
				[ "FLOOR", "NUMERIC" ], [ "RAND", "NUMERIC" ],
				[ "REDUCED", "MODIFIER" ], [ "COUNT", "AGGREGATE" ],
				[ "SUM", "AGGREGATE" ], [ "MIN", "AGGREGATE" ], [ "MAX", "AGGREGATE" ],
				[ "AVG", "AGGREGATE" ], [ "SAMPLE", "AGGREGATE" ],
				[ "GROUP_CONCAT", "AGGREGATE" ] ];

		this.sparql11Update = [ [ "BASE", "" ], [ "PREFIX", "" ], [ "SELECT", "" ],
				[ "DISTINCT", "MODIFIER" ], [ "REDUCED", "" ], [ "NAMED", "" ],
				[ "WHERE", "" ], [ "GRAPH", "" ], [ "TO", "UPDATE" ], [ "USING", "" ],
				[ "DEFAULT", "" ], [ "ALL", "" ], [ "UNION", "" ], [ "FILTER", "" ],
				[ "OPTIONAL", "" ], [ "ORDER", "MODIFIER" ], [ "LIMIT", "MODIFIER" ],
				[ "OFFSET", "MODIFIER" ], [ "BY", "MODIFIER" ], [ "ASC", "" ],
				[ "DESC", "" ], [ "STR", "STRING" ], [ "LANG", "" ],
				[ "LANGMATCHES", "STRING" ], [ "DATATYPE", "" ], [ "BOUND", "" ],
				[ "SAMETERM", "" ], [ "ISIRI", "TERM" ], [ "ISURI", "TERM" ],
				[ "ISBLANK", "TERM" ], [ "ISLITERAL", "TERM" ], [ "REGEX", "STRING" ],
				[ "HAVING", "" ], [ "GROUP", "" ], [ "VALUES", "" ], [ "IF", "" ],
				[ "COALESCE", "" ], [ "EXISTS", "" ], [ "NOT", "" ],
				[ "ISNUMERIC", "TERM" ], [ "IRI", "TERM" ], [ "BNODE", "TERM" ],
				[ "STRDT", "TERM" ], [ "STRLANG", "TERM" ], [ "UUID", "TERM" ],
				[ "STRUUID", "TERM" ], [ "STRLEN", "STRING" ], [ "SUBSTR", "STRING" ],
				[ "LCASE", "STRING" ], [ "UCASE", "STRING" ],
				[ "STRSTARTS", "STRING" ], [ "STRENDS", "STRING" ],
				[ "CONTAINS", "STRING" ], [ "STRBEFORE", "STRING" ],
				[ "STRAFTER", "STRING" ], [ "ENCODE_FOR_URI", "STRING" ],
				[ "CONCAT", "STRING" ], [ "REPLACE", "STRING" ], [ "NOW", "DATE" ],
				[ "YEAR", "DATE" ], [ "MONTH", "DATE" ], [ "DAY", "DATE" ],
				[ "HOURS", "DATE" ], [ "MINUTES", "DATE" ], [ "SECONDS", "DATE" ],
				[ "TIMEZONE", "DATE" ], [ "TZ", "DATE" ], [ "MD5", "HASH" ],
				[ "SHA1", "HASH" ], [ "SHA256", "HASH" ], [ "SHA384", "HASH" ],
				[ "SHA512", "HASH" ], [ "ABS", "NUMERIC" ], [ "ROUND", "NUMERIC" ],
				[ "CEIL", "NUMERIC" ], [ "FLOOR", "NUMERIC" ], [ "RAND", "NUMERIC" ],
				[ "REDUCED", "MODIFIER" ], [ "COUNT", "AGGREGATE" ],
				[ "SUM", "AGGREGATE" ], [ "MIN", "AGGREGATE" ], [ "MAX", "AGGREGATE" ],
				[ "AVG", "AGGREGATE" ], [ "SAMPLE", "AGGREGATE" ],
				[ "GROUP_CONCAT", "AGGREGATE" ], [ "DATA", "UPDATE" ],
				[ "INSERT", "UPDATE" ], [ "DELETE", "UPDATE" ], [ "CREATE", "UPDATE" ],
				[ "DROP", "UPDATE" ], [ "COPY", "UPDATE" ], [ "MOVE", "UPDATE" ],
				[ "ADD", "UPDATE" ], [ "LOAD", "UPDATE" ], [ "INTO", "UPDATE" ],
				[ "WITH", "UPDATE" ], [ "SILENT", "UPDATE" ] ];

		try {
			// Path to images directory
			this.getImagesPath = function() {
				return imagesPath;
			};

			// Returns the version of the software
			this.getVersion = function() {
				return "1.0.3";
			};

			// Returns the title of the software
			this.getTitle = function() {
				return "Flint SPARQL Editor";
			};

			if ($.browser.msie) {
				$('#' + container).append(
						"<form id='" + editorId + "' action='" + config.endpoints[0].uri
								+ "' method='post'></form>");
			} else {
				$('#' + container).append("<div id='" + editorId + "'></div>");
			}

			$('#' + editorId).append(
					"<h1 id='flint-title'>" + this.getTitle() + " " + this.getVersion()
							+ "</h1>");

			// Add menu
			if (config.interface.menu) {
				createMenu = new FlintMenu(flint);
				createMenu.display(editorId);
				this.getMenu = function() {
					return createMenu;
				};
			}

			// Add toolbar
			if (config.interface.toolbar) {
				createToolbar = new FlintToolbar(flint);
				createToolbar.display(editorId);
				this.getToolbar = function() {
					return createToolbar;
				};
			}

			var createSidebar = new FlintSidebar(flint, config);
			var createEndpointBar = new FlintEndpointBar(config, flint);
			var endpointItem = createEndpointBar.getItems()[0], endpointGetInfoButton = createEndpointBar
					.getItems()[2], endpointMimeTypeItem = createEndpointBar.getItems()[3];

			// Add endpoint bar
			try {
				createEndpointBar.display(editorId);
				this.getEndpointBar = function() {
					return createEndpointBar;
				};

				endpointMimeTypeItem.setQueryType("SELECT");

				endpointGetInfoButton
						.setClickAction(function() {
							try {
								var endpointUrl = endpointItem.getEndpoint();
								// If we haven't already retrieved the data
								// prompt
								if (endpointItem.getItem(endpointUrl) === null) {
									flint.getConfirmDialog().setCloseAction(function() {
										if (flint.getConfirmDialog().getResult() === "Okay") {
											// We'll store the
											// data against the
											// endpoint URL
											endpointItem.addItem();
											if (!$.browser.msie) {
												var epItem = endpointItem.getItem(endpointUrl);
												createSidebar.updateProperties(epItem);
												createSidebar.updateClasses(epItem);
												createSidebar.updateSamples(epItem);
											}
										}
									});
									flint
											.getConfirmDialog()
											.show(
													"Flint Error",
													"<p>This operation may take a long time to perform if the dataset contains a large amount of results.</p><p>Do  you want to continue?</p>");
								}
							} catch (e) {
								errorBox.show("Get Dataset Info: " + e);
							}
						});
			} catch (e) {
				errorBox.show(e);
			}

			// Add coolbar
			var createCoolbar = new FlintCoolbar(config, flint);
			createCoolbar.display(editorId);
			this.getCoolbar = function() {
				return createCoolbar;
			};

			// Add sidebar
			createSidebar.display(editorId);
			createSidebar.showActiveTab();
			this.getSidebar = function() {
				return createSidebar;
			};

			// Set mode related items on toolbar and menu
			this.setModeRelatedItems = function(mode) {

				if (mode === "sparql10" || mode === "sparql11query") {
					if (createToolbar) {
						createToolbar.setEnabled("Select", true);
						createToolbar.setEnabled("Construct", true);
						createToolbar.setEnabled("Insert", false);
						createToolbar.setEnabled("Delete", false);
					}
					if (createMenu) {
						createMenu.setEnabled("SelectQuery", true);
						createMenu.setEnabled("ConstructQuery", true);
						createMenu.setEnabled("InsertQuery", false);
						createMenu.setEnabled("DeleteQuery", false);
					}
				}

				if (mode === "sparql11update") {
					if (createToolbar) {
						createToolbar.setEnabled("Select", false);
						createToolbar.setEnabled("Construct", false);
						createToolbar.setEnabled("Insert", true);
						createToolbar.setEnabled("Delete", true);
					}
					if (createMenu) {
						createMenu.setEnabled("SelectQuery", false);
						createMenu.setEnabled("ConstructQuery", false);
						createMenu.setEnabled("InsertQuery", true);
						createMenu.setEnabled("DeleteQuery", true);
					}
				}
			};

			// Get a handle to the coolbar mode picker item
			var coolbarModeItems = createCoolbar.getItems()[1];
			coolbarModeItems.setChangeAction(function() {
				cm.setCodeMode(coolbarModeItems.getMode());
				flint.setModeRelatedItems(coolbarModeItems.getMode());
				// For updates results formats are implementation specific so
				// don't give option
				if (coolbarModeItems.getMode() === "sparql11update") {
					createCoolbar.getItems()[3].disable();
				} else {
					createCoolbar.getItems()[3].enable();
				}
			});

			// Get a handle to the endpoint bar mode picker item
			var endpointBarModeItems = createEndpointBar.getItems()[4];
			endpointBarModeItems.setChangeAction(function() {
				cm.setCodeMode(endpointBarModeItems.getMode());
				flint.setModeRelatedItems(endpointBarModeItems.getMode());
				// For updates results formats are implementation specific so
				// don't give option
				if (endpointBarModeItems.getMode() === "sparql11update") {
					createEndpointBar.getItems()[3].disable();
				} else {
					createEndpointBar.getItems()[3].enable();
				}
			});

			// Get a handle to the dataset item
			var datasetItems = createCoolbar.getItems()[0];

			datasetItems.setChangeAction(function() {
				if ($.browser.msie) {
					$('#' + editorId).attr('action', datasetItems.getEndpoint());
				} else {
					// Update necessary items with data from configuration
					coolbarModeItems.updateModes(datasetItems.getItem(datasetItems
							.getEndpoint()));
					createSidebar.updateProperties(datasetItems.getItem(datasetItems
							.getEndpoint()));
					createSidebar.updateClasses(datasetItems.getItem(datasetItems
							.getEndpoint()));
					createSidebar.updateSamples(datasetItems.getItem(datasetItems
							.getEndpoint()));
				}
			});

			// Get a handle to the formats bar
			var datasetMimeTypeItem = createCoolbar.getItems()[3];
			datasetMimeTypeItem.setQueryType("SELECT");

			// Add about box
			var aboutBox = new FlintAbout(flint);

			// Add confirmation dialog
			var confirmDialog = new FlintDialog();
			confirmDialog.display(editorId);
			this.getConfirmDialog = function() {
				return confirmDialog;
			};

			// Add results area
			var resultsArea;
			if (!$.browser.msie) {
				resultsArea = new FlintResults(flint);
			}

			// Get a handle to the submit button
			var submitItemCoolbar = createCoolbar.getItems()[2];
			var submitItemEndpointBar = createEndpointBar.getItems()[1];

			// Physically set for now but we want this in the configuration so
			// users can override and provide custom submissions
			this.sendDatasetQuery = function() {
				try {
					if (!$.browser.msie) {
						resultsArea.setResults("");
						resultsArea.showLoading(true);
					}
					// Collect query parameters
					var paramsData = {};
					var paramsDataItem = config.defaultEndpointParameters.queryParameters.query;
					if (cm.getQueryType() == 'UPDATE') {
						paramsDataItem = config.defaultEndpointParameters.queryParameters.update;
					}
					paramsData[paramsDataItem] = cm.getCodeMirror().getValue();
					var mimeType = datasetMimeTypeItem.getMimeType();
					$.ajax({
						url : datasetItems.getEndpoint(),
						type : 'post',
						data : paramsData,
						headers : {
							"Accept" : mimeType
						},
						dataType : 'text',
						error : function(XMLHttpRequest, textStatus, errorThrown) {
							if (XMLHttpRequest.status == 0) {
								errorBox.show("The request was not sent. You may be offline");
							} else {
								errorBox.show("Dataset Request: HTTP Status: "
										+ XMLHttpRequest.status + "; " + textStatus);
							}
							resultsArea.showLoading(false);
						},
						success : function(data) {
							resultsArea.setResults(data);
						}
					});
				} catch (e) {
					errorBox.show("Cannot send dataset query: " + e);
				}
			};

			this.sendEndpointQuery = function() {
				try {
					if (!$.browser.msie) {
						resultsArea.setResults("");
						resultsArea.showLoading(true);
					}
					// Collect query parameters
					var paramsData = {};
					var paramsDataItem = config.defaultEndpointParameters.queryParameters.query;
					if (cm.getQueryType() == 'UPDATE') {
						paramsDataItem = config.defaultEndpointParameters.queryParameters.update;
					}
					paramsData[paramsDataItem] = cm.getCodeMirror().getValue();
					var mimeType = endpointMimeTypeItem.getMimeType();
					$.ajax({
						url : endpointItem.getEndpoint(),
						type : 'post',
						data : paramsData,
						headers : {
							"Accept" : mimeType
						},
						dataType : 'text',
						error : function(XMLHttpRequest, textStatus, errorThrown) {
							if (XMLHttpRequest.status == 0) {
								errorBox.show("The request was not sent. You may be offline.");
							} else {
								errorBox.show("Endpoint Request: HTTP Status: "
										+ XMLHttpRequest.status + "; " + textStatus);
							}
							resultsArea.showLoading(false);
						},
						success : function(data) {
							resultsArea.setResults(data);
						}
					});
				} catch (e) {
					errorBox.show("Cannot send endpoint query: " + e);
				}
			};

			this.sendIEDatasetQuery = function() {
				$("#" + editorId).attr('action', datasetItems.getEndpoint());
			};

			this.sendIEEndpointQuery = function() {
				$("#" + editorId).attr('action', endpointItem.getEndpoint());
			};

			if (!$.browser.msie) {
				submitItemCoolbar.setSubmitAction(this.sendDatasetQuery);
				submitItemEndpointBar.setSubmitAction(this.sendEndpointQuery);
			} else {
				submitItemCoolbar.setSubmitAction(this.sendIEDatasetQuery);
				submitItemEndpointBar.setSubmitAction(this.sendIEEndpointQuery);
			}

			// Add status area
			var statusArea = new FlintStatus();
			this.getStatusArea = function() {
				return statusArea;
			};

			// Add actual code editing area
			cm = new FlintCodeEditor(flint, "sparql10");
			cm.display(editorId);
			this.getCodeEditor = function() {
				return cm.getCodeMirror();
			};

			statusArea.display(editorId);
			statusArea.updateStatus();
			// Trigger an update to ensure modes and synced with endpoint data
			// item
			coolbarModeItems.updateModes(datasetItems.getItem(datasetItems
					.getEndpoint()));

			// Add tab to editor
			this.addTab = function() {
				cm.addTab();
			};

			// Clear the editor area
			this.clearEditorTextArea = function() {
				if (cm.getCodeMirror().getValue() != "") {
					confirmDialog.setCloseAction(function() {
						var result = confirmDialog.getResult();
						if (result == "Okay") {
							cm.getCodeMirror().setValue("");
							cm.getCodeMirror().focus();
						}

					});
					confirmDialog.show("New Query",
							"<p>Are you sure you want to abandon the current text?</p>");
				}
			};

			this.undo = function() {
				cm.getCodeMirror().undo();
			};

			this.redo = function() {
				cm.getCodeMirror().redo();
			};

			this.cut = function() {
				cm.getCodeMirror().replaceSelection("");
				cm.getCodeMirror().focus();
			};

			this.insert = function(text) {
				cm.getCodeMirror().replaceSelection(text);
				cm.getCodeMirror().focus();
			};

			this.toggleTools = function() {
				$('#flint-sidebar-grabber').click();
			};

			this.showEndpointBar = function() {
				// Important to clear this in case any running HTTP requests
				// have not been finished before switching
				createSidebar.clearActiveItem();
				createSidebar.showActiveTab();
				createCoolbar.hide();
				createEndpointBar.show();
				createToolbar.setEnabled("Show Endpoints", false);
				createToolbar.setEnabled("Show Datasets", true);
				createMenu.setEnabled("Show Endpoints", false);
				createMenu.setEnabled("Show Datasets", true);
				cm.setCodeMode(endpointBarModeItems.getMode());
				var endpointItem = createEndpointBar.getItems()[0];
				var endpointUrl = endpointItem.getEndpoint();
				var item = endpointItem.getItem(endpointUrl);
				createSidebar.updateProperties(item);
				createSidebar.updateClasses(item);
				createSidebar.updateSamples(item);
			};

			this.showDataSetsBar = function() {
				// Important to clear this in case any running HTTP requests
				// have not been finished before switching
				createSidebar.clearActiveItem();
				createSidebar.showActiveTab();
				createCoolbar.show();
				createEndpointBar.hide();
				createToolbar.setEnabled("Show Endpoints", true);
				createToolbar.setEnabled("Show Datasets", false);
				createMenu.setEnabled("Show Endpoints", true);
				createMenu.setEnabled("Show Datasets", false);
				cm.setCodeMode(coolbarModeItems.getMode());
				var item = datasetItems.getItem(datasetItems.getEndpoint());
				createSidebar.updateProperties(item);
				createSidebar.updateClasses(item);
				createSidebar.updateSamples(item);
			};

			this.formatQuery = function() {
				var maxlines = cm.getCodeMirror().lineCount();
				var ln;
				for (ln = 0; ln < maxlines; ++ln) {
					cm.getCodeMirror().indentLine(ln);
				}
			};

			this.insertQuery = function(title, query, line, ch) {
				if (cm.getCodeMirror().getValue() != "") {
					confirmDialog.setCloseAction(function() {
						var result = confirmDialog.getResult();
						if (result == "Okay") {
							cm.getCodeMirror().setValue(
									createSidebar.getPrefixes() + "\n" + query);
							cm.getCodeMirror().setCursor(
									line + createSidebar.getPrefixCount(), ch);
							flint.formatQuery();
							cm.getCodeMirror().focus();
						}
					});
					confirmDialog.show(title,
							"<p>Are you sure you want to abandon the current text?</p>");
				} else {
					cm.getCodeMirror().setValue(
							createSidebar.getPrefixes() + "\n" + query);
					cm.getCodeMirror().setCursor(line + createSidebar.getPrefixCount(),
							ch);
					this.formatQuery();
					cm.getCodeMirror().focus();
				}
			};

			this.insertSelectQuery = function() {
				this.insertQuery("New Select Query",
						"SELECT * WHERE {\n?s ?p ?o\n}\nLIMIT 10", 1, 7);
			};

			this.insertConstructQuery = function() {
				this.insertQuery("New Construct Query",
						"CONSTRUCT {\n?s ?p ?o\n} WHERE {\n?s ?p ?o\n}\nLIMIT 10", 2, 0);
			};

			this.insertInsertQuery = function() {
				this.insertQuery("New Insert Query",
						"INSERT DATA {\nGRAPH <>\n\t{\n\t\t\n\t}\n}", 2, 7);
			};

			this.insertDeleteQuery = function() {
				this
						.insertQuery(
								"New Delete Query",
								"DELETE DATA {\n\t<http://example/book2> dc:title 'David Copperfield';\n\t\tdc:creator 'Edmund Wells' .\n}",
								2, 1);
			};

			this.showAbout = function() {
				aboutBox.show();
			};

			// Handle window resizing
			$(window).resize(function() {
				// Resize editing area. Should this be in the
				// FlintCodeEditor object?
				// Will be triggered by window resize and sidebar
				// display/hiding
				var editorWidth = $('#flint-editor').width();
				if (!createSidebar.visible()) {
					$('.CodeMirror').css("width", (editorWidth - 55) + "px");
				} else {
					$('#flint-sidebar').css("width", editorWidth / 2 + "px");
					$('.CodeMirror').css("width", ((editorWidth / 2) - 25) + "px");
				}
				cm.resize();
			});

			if (!$.browser.msie) {
				resultsArea.display(editorId);
				try {
					createSidebar.updateProperties(datasetItems.getItem(datasetItems
							.getEndpoint()));
					createSidebar.updateClasses(datasetItems.getItem(datasetItems
							.getEndpoint()));
					createSidebar.updateSamples(datasetItems.getItem(datasetItems
							.getEndpoint()));
				} catch (e) {
					errorBox.show(e);
				}
			}

			// Force components to get to their required size
			$(window).resize();
		} catch (e) {
			window.alert(e);
		}
	}

	editor = new Flint(container, imagesPath, config);

	this.getEditor = function() {
		return editor;
	};

}
