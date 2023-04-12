package fr.inria.corese.command.programs;

import java.nio.file.Path;
import java.security.InvalidParameterException;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.InputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.OWLProfile;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Profile
 */
@Command(name = "owlProfile", version = App.version, description = "Check OWL profiles.", mixinStandardHelpOptions = true)
public class Profile implements Runnable {

    @Parameters(paramLabel = "INPUT_FORMAT", description = "Input file format."
            + " Candidates: ${COMPLETION-CANDIDATES}")
    private InputFormat inputFormat;

    @Parameters(paramLabel = "INPUT", description = "Input file path.")
    private String intputPath;

    @Parameters(paramLabel = "OWL_PROFILE", description = "OWL profile to check."
            + " Candidates: ${COMPLETION-CANDIDATES}")
    private OwlProfile owlProfile;

    private Graph graph;

    public Profile() {
    }

    /**
     * Enumeration of OWL profiles.
     */
    private enum OwlProfile {
        OWL_EL,
        OWL_QL,
        OWL_RL,
        OWL, // tests the validity of OWL 2
    }

    @Override
    public void run() {
        this.graph = GraphUtils.load(this.intputPath, this.inputFormat);
        chechProfile();
    }

    /**
     * Convert {@code fr.inria.corese.programs.Profile.OwlProfile} value into
     * {@code fr.inria.corese.core.logic.OWLProfile} equivalent value.
     */
    private void chechProfile() {

        OWLProfile tc = new OWLProfile(graph);
        boolean suc = false;
        try {
            switch (this.owlProfile) {
                case OWL_EL:
                    suc = tc.process(OWLProfile.OWL_EL);
                    break;

                case OWL_QL:
                    suc = tc.process(OWLProfile.OWL_QL);
                    break;

                case OWL_RL:
                    suc = tc.process(OWLProfile.OWL_RL);
                    break;

                case OWL:
                    suc = tc.process(OWLProfile.OWL_TC);
                    break;

                default:
                    throw new InvalidParameterException("Profile " + this.owlProfile + " is unknow.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // error messages:
        String mes = tc.getMessage();

        System.out.println(suc);

        if (!suc) {
            System.out.println(mes);
        }
    }
}