module fr.inria.corese.kgram {
	requires java.desktop;
	requires org.slf4j;

	exports fr.inria.corese.kgram.core;
	exports fr.inria.corese.kgram.api.core;
	exports fr.inria.corese.kgram.api.query;
	exports fr.inria.corese.kgram.filter;
	exports fr.inria.corese.kgram.event;
	exports fr.inria.corese.kgram.tool;
	exports fr.inria.corese.kgram.sorter.core;
	exports fr.inria.corese.kgram.path;
	exports fr.inria.corese.kgram.sorter.impl.qpv1;
}
