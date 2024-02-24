import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Decoder {

    GenericPackager packager;
    byte[] isoMessageBytes;
    ArrayList<String> decodedMessage = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public Decoder(String packager, String isoMessageBytes) {

        Client.fileHandler.setLevel(Level.ALL);
        logger.addHandler(Client.fileHandler);

        try {
            this.packager = new GenericPackager(packager);
            this.isoMessageBytes = ISOUtil.hex2byte(isoMessageBytes);
        } catch (ISOException e) {
            logger.log(Level.SEVERE, "Error creating Decoder: " + e.getMessage(), e);
        }
    }

    synchronized public void Decode() {
        try {
            // Create an empty ISO message
            ISOMsg isoMsg = new ISOMsg();

            // Unpack the ISO message
            isoMsg.setPackager(packager);
            isoMsg.unpack(isoMessageBytes);

            // Display the decoded fields
            for (int i = 1; i <= isoMsg.getMaxField(); i++) {
                if (isoMsg.hasField(i)) {
                    if (isoMsg.getString(i) == null) {
                        ISOMsg subfield = (ISOMsg) isoMsg.getValue(i);
                        for (int j = 1; j <= subfield.getMaxField(); j++) {
                            if (subfield.hasField(j)) {
                                String decoded = "Field " + i + "." + j + ": " + subfield.getString(j) + "\n";
                                decodedMessage.add(decoded);
                            }
                        }
                    } else {
                        String decoded = "Field " + i + ": " + isoMsg.getString(i) + "\n";
                        decodedMessage.add(decoded);
                    }
                }
            }
        } catch (ISOException e) {
            logger.log(Level.SEVERE, "Error decoding ISO message: " + e.getMessage(), e);
        }
    }

    public List<String> getDecodedMsg() {
        Decode();
        return decodedMessage;
    }

}
