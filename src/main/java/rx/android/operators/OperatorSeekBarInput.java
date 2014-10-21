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
package rx.android.operators;

import android.widget.SeekBar;
import rx.Observable;
import rx.Subscriber;
import rx.android.events.OnSeekBarEvent;
import rx.android.observables.Assertions;
import rx.android.subscriptions.AndroidSubscriptions;
import rx.functions.Action0;

import java.util.HashSet;
import java.util.Set;

import static rx.android.events.OnSeekBarEvent.EventType;

public class OperatorSeekBarInput implements Observable.OnSubscribe<OnSeekBarEvent> {
    public static final int LISTENER_TAG = 0xBDD2ADD1;
    private final SeekBar seekBar;
    private final boolean emitInitialValue;

    public OperatorSeekBarInput(final SeekBar seekBar, final boolean emitInitialValue) {
        this.seekBar = seekBar;
        this.emitInitialValue = emitInitialValue;
    }

    @Override
    public void call(final Subscriber<? super OnSeekBarEvent> subscriber) {
        Assertions.assertUiThread();
        final ObservableListener listener = getListener();
        listener.addSubscriber(subscriber);

        subscriber.add(AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                listener.unsubscribe(subscriber);
            }
        }));

        if (emitInitialValue) {
            subscriber.onNext(new OnSeekBarEvent(seekBar, seekBar.getProgress(), false, EventType.PROGRESS_CHANGED));
        }
        seekBar.setOnSeekBarChangeListener(listener);
    }

    private ObservableListener getListener() {
        Object tag = seekBar.getTag(LISTENER_TAG);
        if (!(tag instanceof ObservableListener)) {
            tag = new ObservableListener();
            seekBar.setTag(LISTENER_TAG, tag);
        }
        return (ObservableListener) tag;
    }

    private class ObservableListener implements SeekBar.OnSeekBarChangeListener {
        private final Set<Subscriber<? super OnSeekBarEvent>> subscribers = new HashSet<Subscriber<? super OnSeekBarEvent>>();

        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
            for (Subscriber<? super OnSeekBarEvent> subscriber : subscribers) {
                subscriber.onNext(new OnSeekBarEvent(seekBar, progress, fromUser, EventType.PROGRESS_CHANGED));
            }
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
            for (Subscriber<? super OnSeekBarEvent> subscriber : subscribers) {
                subscriber.onNext(new OnSeekBarEvent(seekBar, seekBar.getProgress(), false, EventType.START_TRACKING_TOUCH));
            }
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            for (Subscriber<? super OnSeekBarEvent> subscriber : subscribers) {
                subscriber.onNext(new OnSeekBarEvent(seekBar, seekBar.getProgress(), false, EventType.STOP_TRACKING_TOUCH));
            }
        }

        public void addSubscriber(final Subscriber<? super OnSeekBarEvent> subscriber) {
            subscribers.add(subscriber);
        }

        public void unsubscribe(final Subscriber<? super OnSeekBarEvent> unsubscriber) {
            Assertions.assertUiThread();
            subscribers.remove(unsubscriber);
            if (subscribers.isEmpty()) {
                seekBar.setOnSeekBarChangeListener(null);
                seekBar.setTag(LISTENER_TAG, null);
            }
        }
    }
}
