package fr.inria.corese.command.utils;

import java.security.InvalidParameterException;

import fr.inria.corese.command.utils.format.InputFormat;
import fr.inria.corese.command.utils.format.OutputFormat;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.transform.Transformer;

public class FromatManager {

    private FromatManager() {
    }

    /**
     * Convert {@code OutputFormat} value into {@code Transformer} equivalent value.
     * 
     * @param outputFormat Value to convert.
     * @return Converted value.
     */
    public static String getCoreseOutputFormat(OutputFormat outputFormat) {
        switch (outputFormat) {
            case RDFXML:
                return Transformer.RDFXML;

            case TURTLE:
                return Transformer.TURTLE;

            case JSONLD:
                return Transformer.JSON;

            case TRIG:
                return Transformer.TRIG;

            default:
                throw new InvalidParameterException("Output format " + outputFormat + " is unknow.");
        }
    }

    /**
     * Convert {@code OutputFormat} value into {@code Loader} equivalent value.
     * 
     * @param inputFormat Value to convert.
     * @return Converted value.
     */
    public static int getCoreseinputFormat(InputFormat inputFormat) {
        switch (inputFormat) {
            case RDFXML:
                return Loader.RDFXML_FORMAT;

            case TURTLE:
                return Loader.TURTLE_FORMAT;

            case N3:
            case NTRIPLES:
                return Loader.NT_FORMAT;

            case JSONLD:
                return Loader.JSONLD_FORMAT;

            case TRIG:
                return Loader.TRIG_FORMAT;

            case NQUADS:
                return Loader.NQUADS_FORMAT;

            case RDFA:
                return Loader.RDFA_FORMAT;

            default:
                throw new InvalidParameterException("Input format " + inputFormat + " is unknow.");
        }
    }

}
