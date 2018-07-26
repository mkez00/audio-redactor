package com.mkez00.audioredactor.service;

import com.mkez00.audioredactor.exception.BadRequestException;
import com.mkez00.audioredactor.helper.FileHelper;
import com.mkez00.audioredactor.model.Audio;
import com.mkez00.audioredactor.model.BaseAudio;
import com.mkez00.audioredactor.model.RedactionSegment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.swing.text.Segment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AudioRedactionFfmpegServiceImpl implements AudioRedactionService{

    protected final Log LOG = LogFactory.getLog(this.getClass());

    @Value("${delete.temp.source.media}")
    boolean deleteTempSourceMedia;

    @Value("${delete.temp.destination.media}")
    boolean deleteTempDestinationMedia;

    @Value("${destination.media.file.location}")
    String destinationMediaFileLocation;

    @Override
    public BaseAudio redact(Audio audio) throws IOException, InterruptedException{
        File audioFile = FileHelper.decodeBase64AndWriteToFile(audio.getPayload());
        //check if the hash is valid
        if (!isValidHash(audio, audioFile)){
            throw new BadRequestException("Invalid Hash Provided");
        }
        String destination = getDestinationFileLocation(audio);
        LOG.info("Source file created at: " + audioFile.getAbsolutePath());
        LOG.info("Destination to be created at: " + destination);
        monitorSystemCall(Runtime.getRuntime().exec(getCommand(audio, audioFile.getAbsolutePath(), destination)));
        return buildReturnAndClean(audioFile, destination);
    }

    /**
     *
     * Determine the absolute path of the destination file
     *
     * @param audio
     * @return
     */
    private String getDestinationFileLocation(Audio audio){
        String destination = getDestinationDirectory();
        destination += getDestinationFileName(audio);
        return destination;
    }

    /**
     * If the client provides the destination file name then use the provided name
     *
     * @param audio
     * @return
     */
    private String getDestinationFileName(Audio audio){
        if (audio!=null  && audio.getDestinationFileName()!=null && !audio.getDestinationFileName().isEmpty()){
            return audio.getDestinationFileName();
        } else {
            return UUID.randomUUID().toString() + ".mp3";
        }
    }

    /**
     * If the service is configured to use an alternate file location, use that.  Otherwise use the Java temp directory
     *
     * @return
     */
    private String getDestinationDirectory(){
        if (destinationMediaFileLocation!=null && !destinationMediaFileLocation.isEmpty()){
            return addDelimeterToEndOfString(destinationMediaFileLocation);
        } else {
            return addDelimeterToEndOfString(FileHelper.getTempDirectory());
        }
    }

    /**
     * If the client does not include the file separator at the end of the string.  Add manually
     *
     * @param value
     * @return
     */
    private String addDelimeterToEndOfString(String value){
        if (value!=null && !value.isEmpty()){
            if (!value.substring(value.length()-1).equalsIgnoreCase(System.getProperty("file.separator"))){
                value += System.getProperty("file.separator");
                return value;
            }
        }
        return value;
    }

    /**
     * Waits for system call to finish logging success or fail message
     *
     * @param p
     * @throws IOException
     * @throws InterruptedException
     */
    private void monitorSystemCall(Process p) throws IOException, InterruptedException{
        //Wait to get exit value
        p.waitFor();
        final int exitValue = p.waitFor();
        if (exitValue == 0)
            LOG.info("Successfully redacted audio segments from provided source!!!");
        else {
            LOG.info("Failed to execute the following command: due to the following error(s):");
            final BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            if ((line = b.readLine()) != null) {
                LOG.info(line);
            }
        }
    }

    /**
     * Build return object and delete created temp files (if deletion enabled)
     *
     * @param audioFile
     * @param destination
     * @return
     * @throws IOException
     */
    private BaseAudio buildReturnAndClean(File audioFile, String destination) throws IOException{
        BaseAudio returnAudio = new BaseAudio();
        File destinationFile = new File(destination);
        returnAudio.setPayload(FileHelper.encodeBase64(destinationFile));
        returnAudio.setSha256(FileHelper.getSha256(destinationFile));
        FileHelper.deleteFiles(deleteTempDestinationMedia ? destination : null, deleteTempSourceMedia ? audioFile.getAbsolutePath() : null);
        return returnAudio;
    }

    /**
     * Validate hash provided by client
     *
     * @param audio
     * @param audioFile
     * @return
     * @throws IOException
     */
    private boolean isValidHash(Audio audio, File audioFile) throws IOException {
        if (audio!=null && audio.getSha256()!=null && !audio.getSha256().isEmpty()){
            String audioFileHash = FileHelper.getSha256(audioFile);
            if (audio.getSha256().equalsIgnoreCase(audioFileHash)){
                LOG.info("Valid SHA256: " + audio.getSha256());
                return true;
            } else {
                LOG.info("Invalid SHA256: " + audio.getSha256());
                return false;
            }
        } else {
            LOG.info("SHA256 not provided.  Skipping validation.");
            return true;
        }
    }

    /**
     *
     * Builds command to call to system which must have FFMPEG installed to work.  The call itself will take the source file and strip out the
     * segments that are provided in the metadata (Audio)
     *
     * @param audio
     * @param audioSourceFilePath
     * @param audioDestinationFilePath
     * @return
     */
    private String[] getCommand(Audio audio, String audioSourceFilePath, String audioDestinationFilePath){
        List<String> command = new ArrayList<>();

        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(audioSourceFilePath);
        command.add("-af");

        if (audio.getSegments()!=null) {
            String parameters = "";
            for (RedactionSegment redactionSegment : audio.getSegments()){
                LOG.info("Redacting segment from " + redactionSegment.getStartSecond() + " (sec) to " + redactionSegment.getEndSecond() + " (sec)");
                String parameter = "volume=enable='between(t," + redactionSegment.getStartSecond() + "," + redactionSegment.getEndSecond() + ")':volume=0,";
                parameters += parameter;
            }
            parameters = parameters.substring(0, parameters.length()-1);
            command.add(parameters);
        }
        command.add(audioDestinationFilePath);

        String[] simpleArray = new String[ command.size() ];
        command.toArray( simpleArray );
        return simpleArray;
    }
}
