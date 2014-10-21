/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.android.events;

import android.widget.SeekBar;

public class OnSeekBarEvent {
    public final SeekBar seekBar;
    public final int progress;
    public final boolean fromUser;
    private final EventType eventType;

    public OnSeekBarEvent(final SeekBar seekBar, final int progress, final boolean fromUser, final EventType eventType) {
        this.seekBar = seekBar;
        this.progress = progress;
        this.fromUser = fromUser;
        this.eventType = eventType;
    }

    public boolean is(EventType et) {
        return eventType == et;
    }

    public enum EventType {
        START_TRACKING_TOUCH,
        STOP_TRACKING_TOUCH,
        PROGRESS_CHANGED
    }
}
