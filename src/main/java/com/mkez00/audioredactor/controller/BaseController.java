package com.mkez00.audioredactor.controller;

import com.mkez00.audioredactor.exception.InternalServerException;
import com.mkez00.audioredactor.model.Audio;
import com.mkez00.audioredactor.model.BaseAudio;
import com.mkez00.audioredactor.service.AudioRedactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class BaseController {

    protected final Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    AudioRedactionService audioRedactionService;

    @RequestMapping(path="/", method= RequestMethod.POST)
    public BaseAudio redactAudio(@RequestBody Audio audio){
        try {
            return audioRedactionService.redact(audio);
        } catch (IOException | InterruptedException e) {
            LOG.error("Error processing request",e);
            throw new InternalServerException(e.getMessage());
        }
    }
}
