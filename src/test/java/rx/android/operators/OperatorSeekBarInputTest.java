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

import android.app.Activity;
import android.widget.SeekBar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.events.OnSeekBarEvent;
import rx.android.observables.ViewObservable;
import rx.observers.TestObserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@RunWith(RobolectricTestRunner.class)
public class OperatorSeekBarInputTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testOverloadedMethodDefaultsWithoutInitialValue() {
        final SeekBar input = mkSeekBar(15);
        final Observable<OnSeekBarEvent> observable = ViewObservable.input(input);
        final Observer<OnSeekBarEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnSeekBarEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnSeekBarEvent.class));

        input.setProgress(1);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 1));

        input.setProgress(2);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 2));

        input.setProgress(3);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 3));

        subscription.unsubscribe();
        input.setProgress(4);
        inOrder.verify(observer, never()).onNext(any(OnSeekBarEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    private static SeekBar mkSeekBar(final int value) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final SeekBar bar = new SeekBar(activity);
        bar.setProgress(value);
        return bar;
    }

    private static OnSeekBarEvent mkMockedEvent(final SeekBar view, final int progress) {
        return argThat(new ArgumentMatcher<OnSeekBarEvent>() {
            @Override
            public boolean matches(final Object argument) {
                if (!(argument instanceof OnSeekBarEvent)) {
                    return false;
                }
                final OnSeekBarEvent event = (OnSeekBarEvent) argument;
                return event.seekBar == view && event.progress == progress;
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithoutInitialValue() {
        final SeekBar input = mkSeekBar(0);
        final Observable<OnSeekBarEvent> observable = ViewObservable.input(input, false);
        final Observer<OnSeekBarEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnSeekBarEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnSeekBarEvent.class));

        input.setProgress(1);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 1));

        input.setProgress(2);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 2));

        input.setProgress(3);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 3));

        subscription.unsubscribe();
        input.setProgress(4);
        inOrder.verify(observer, never()).onNext(any(OnSeekBarEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithInitialValue() {
        final SeekBar input = mkSeekBar(0);
        final Observable<OnSeekBarEvent> observable = ViewObservable.input(input, true);
        final Observer<OnSeekBarEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnSeekBarEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 0));

        input.setProgress(1);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 1));

        input.setProgress(2);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 2));

        input.setProgress(3);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, 3));

        subscription.unsubscribe();
        input.setProgress(4);
        inOrder.verify(observer, never()).onNext(any(OnSeekBarEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleSubscriptions() {
        final SeekBar input = mkSeekBar(0);
        final Observable<OnSeekBarEvent> observable = ViewObservable.input(input, false);

        final Observer<OnSeekBarEvent> observer1 = mock(Observer.class);
        final Observer<OnSeekBarEvent> observer2 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<OnSeekBarEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnSeekBarEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        input.setProgress(1);
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(input, 1));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(input, 1));

        input.setProgress(2);
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(input, 2));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(input, 2));
        subscription1.unsubscribe();

        input.setProgress(3);
        inOrder1.verify(observer1, never()).onNext(any(OnSeekBarEvent.class));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(input, 3));
        subscription2.unsubscribe();

        input.setProgress(4);
        inOrder1.verify(observer1, never()).onNext(any(OnSeekBarEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnSeekBarEvent.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }
}
