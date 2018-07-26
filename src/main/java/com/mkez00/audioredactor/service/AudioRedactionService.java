package com.mkez00.audioredactor.service;

import com.mkez00.audioredactor.model.Audio;
import com.mkez00.audioredactor.model.BaseAudio;

import java.io.IOException;

public interface AudioRedactionService {
    BaseAudio redact(Audio audio) throws IOException, InterruptedException;
}
