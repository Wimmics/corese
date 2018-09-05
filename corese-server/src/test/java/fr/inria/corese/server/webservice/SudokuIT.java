package fr.inria.corese.server.webservice;

import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SudokuIT {

	private WebDriver driver;

	@BeforeClass
	public void before() {
		WebDriver driver = new FirefoxDriver();
	}

	@AfterClass
	public void after() {
		driver.quit();
	}	
	/**
	 * Check whether the reset button empties the grid.
	 */
	@Test
	public void resetButtonEmptyGrid() {
		driver.navigate().to("http://localhost:8080/");
		WebElement miscMenu = driver.findElement(By.xpath("//a[@id='MiscEntry']"));
		miscMenu.click();
		WebElement sudokuEntry = driver.findElement(By.id("SudokuEntry"));
		sudokuEntry.click();
		WebElement resetButton = driver.findElement(By.id("reset"));
		resetButton.click();
		assertTrue("The sudoku grid should be empty after pressing the reset button.", isGridEmpty());
	}

	@Test
	public void submitButtonNotEmptyGrid() {
		driver.navigate().to("http://localhost:8080/");
		WebElement miscMenu = driver.findElement(By.xpath("//a[@id='MiscEntry']"));
		miscMenu.click();
		WebElement sudokuEntry = driver.findElement(By.id("SudokuEntry"));
		sudokuEntry.click();
		WebElement resetButton = driver.findElement(By.id("submit"));
		resetButton.click();
		assertTrue("The sudoku grid should not be empty after pressing the submit button.", !isGridEmpty());
	}
	
	private boolean isGridEmpty() {
		for (char line = 'a'; line < 'j'; line++) {
			for (int col = 1; col < 10; col++) {
				WebElement tile = driver.findElement(By.id("" + line + col));
				if (!tile.getAttribute("value").isEmpty()) {
					return false;	
				}
			}
		}
		return true;
	}
}
