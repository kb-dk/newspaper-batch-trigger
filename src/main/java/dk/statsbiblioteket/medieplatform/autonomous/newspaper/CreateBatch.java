package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;

import dk.statsbiblioteket.medieplatform.autonomous.Event;
import dk.statsbiblioteket.medieplatform.autonomous.NotFoundException;
import org.slf4j.Logger;

import java.util.Date;


/**
 * Called from shell script with arguments to create a batch object in DOMS with proper Premis event added.
 *
 * @author jrg
 */
public class CreateBatch {
    public static Logger log = org.slf4j.LoggerFactory.getLogger(CreateBatch.class);

    /**
     * Receives the following arguments to create a batch object in DOMS:
     * Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,
     * URL to PID generator.
     *
     * @param args The command line arguments received from calling shell script. Explained above.
     */
    public static void main(String[] args) {
        String batchId;
        String roundTrip;
        String premisAgent;
        String domsUrl;
        String domsUser;
        String domsPass;
        String urlToPidGen;
        DomsEventStorageFactory domsEventStorageFactory = new DomsEventStorageFactory();
        DomsEventStorage domsEventClient;
        Date now = new Date();
        log.info("Entered main");
        if (args.length != 7) {
            System.out.println("Not the right amount of arguments");
            System.out.println("Receives the following arguments (in this order) to create a batch object in DOMS:");
            System.out.println(
                    "Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,");
            System.out.println("URL to PID generator.");
            System.exit(1);
        }
        batchId = args[0];
        roundTrip = args[1];
        premisAgent = args[2];
        domsUrl = args[3];
        domsUser = args[4];
        domsPass = args[5];
        urlToPidGen = args[6];
        domsEventStorageFactory.setFedoraLocation(domsUrl);
        domsEventStorageFactory.setUsername(domsUser);
        domsEventStorageFactory.setPassword(domsPass);
        domsEventStorageFactory.setPidGeneratorLocation(urlToPidGen);
        try {
            domsEventClient = domsEventStorageFactory.createDomsEventStorage();
            final int roundTripNumber = Integer.parseInt(roundTrip);
            boolean success = true;
            String message = "";

            for (int olderRoundTripNumber=0; olderRoundTripNumber<roundTripNumber; olderRoundTripNumber++) {
                try {
                    Batch olderRoundtrip = domsEventClient.getBatch(batchId, olderRoundTripNumber);
                    if (isApproved(olderRoundtrip)) {
                        message  +=  "Batch ("+olderRoundTripNumber+") is already approved, so this roundtrip ("+roundTripNumber+")should not be triggered here\n";
                        success = false;

                    } else if ( isReadyForManualQA(olderRoundtrip)) {
                        message
                                += "Batch (" + olderRoundTripNumber + ") is already in manual qa, so this roundtrip (" + roundTripNumber + ")should not be triggered here\n";

                        success = false;

                    }
                } catch (NotFoundException e) {
                    //ignore, proceed
                }
            }
            domsEventClient.addEventToBatch(batchId, roundTripNumber, premisAgent, now, message, "Data_Received", success);
            if (!success){
                for (int olderRoundTripNumber = 0; olderRoundTripNumber < roundTripNumber; olderRoundTripNumber++) {
                    domsEventClient.addEventToBatch(batchId,
                            olderRoundTripNumber,
                            premisAgent,
                            now,
                            "Newer batch (" + roundTripNumber + ") have been received, so this batch should be stopped",
                            "Manually_stopped",
                            false);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed adding event to batch, due to: " + e.getMessage());
            log.error("Caught exception: ", e);
            System.exit(1);
        }
    }

    private static boolean isReadyForManualQA(Batch olderRoundtrip) {
        boolean approved = false;
        for (Event event : olderRoundtrip.getEventList()) {
            if (event.getEventID().equals("Approved")) {
                approved = true;
                break;
            }
        }
        for (Event event : olderRoundtrip.getEventList()) {
            if (event.getEventID().equals("Manual_QA_Flagged")) {
                return event.isSuccess() && !approved;
            }
        }
        return false;
    }

    private static boolean isApproved(Batch olderRoundtrip) {
        for (Event event : olderRoundtrip.getEventList()) {
            if (event.getEventID().equals("Approved")){
                return event.isSuccess();
            }
        }
        return false;
    }
}
