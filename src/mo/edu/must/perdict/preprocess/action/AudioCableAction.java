package mo.edu.must.perdict.preprocess.action;

import java.util.Date;

public class AudioCableAction extends Action {
    public AudioCableAction(Date time) {
        eventId = EVENT_AUDIO_CABLE;
        this.time = time;
    }

    @Override
    public String toString() {
        return "AudioCableAction[]";
    }
}
