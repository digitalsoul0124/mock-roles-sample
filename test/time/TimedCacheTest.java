package time;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TimedCacheTest {

    /*
     * 仕様：
     * オブジェクトをロードするフレームワークに対してキーを元にした検索を行い、
     * その結果をキャッシュするコンポーネントを考える。
     * ロードされてから一定時間が経つと、そのインスタンスは使えなくなる。
     * そこで、時々リロードをしなければならなくなる。
     * 
     * 参考：http://jmock.org/oopsla2004_ja.pdf
     */
    
    private Mockery context = new Mockery();

    @Test
    public void キャッシュされていないオブジェクトはロードする() throws Exception {

        final ObjectLoader mockLoader = context.mock(ObjectLoader.class);
        final Clock mockClock = context.mock(Clock.class);
        final ReloadPolicy mockPolicy = context.mock(ReloadPolicy.class);

        context.checking(new Expectations() {
            {
                oneOf(mockLoader).load("KEY");
                will(returnValue("VALUE"));

                allowing(mockClock).getCurrentTime();
                will(returnValue(new Timestamp("2011/09/16 00:00:00.000")));
            }
        });

        TimedCache cache = new TimedCache(mockLoader, mockClock, mockPolicy);
        assertThat((String) cache.lookup("KEY"), is("VALUE"));
    }

    @Test
    public void タイムアウト前のキャッシュされたオブジェクトはロードしない() throws Exception {
        // 呼び出しの順番もあわせて定義する

        final Sequence sequence = context.sequence("sequence");

        final Clock mockClock = context.mock(Clock.class);
        final ObjectLoader mockLoader = context.mock(ObjectLoader.class);
        final ReloadPolicy mockPolicy = context.mock(ReloadPolicy.class);

        final Timestamp loadTime = new Timestamp("2011/09/17 00:00:00.000");
        final Timestamp fetchTime = new Timestamp("2011/09/17 00:00:01.000"); // 1秒後

        context.checking(new Expectations() {
            {

                oneOf(mockLoader).load("KEY");
                will(returnValue("VALUE"));
                inSequence(sequence);

                exactly(2).of(mockClock).getCurrentTime();
                will(onConsecutiveCalls(returnValue(loadTime), returnValue(fetchTime)));
                inSequence(sequence);

                atLeast(1).of(mockPolicy).shouldReload(loadTime, fetchTime);
                will(returnValue(false));
            }
        });

        TimedCache cache = new TimedCache(mockLoader, mockClock, mockPolicy);
        assertThat("ロードされたオブジェクト", (String) cache.lookup("KEY"), is("VALUE"));
        assertThat("キャッシュされたオブジェクト", (String) cache.lookup("KEY"), is("VALUE"));
    }

    @Test
    public void タイムアウト後のキャッシュされたオブジェクトはロードする() throws Exception {

        final Clock mockClock = context.mock(Clock.class);
        final ObjectLoader mockLoader = context.mock(ObjectLoader.class);
        final ReloadPolicy mockPolicy = context.mock(ReloadPolicy.class);

        final Timestamp loadTime = new Timestamp("2011/09/17 00:00:00.000");
        final Timestamp fetchTime = new Timestamp("2011/09/17 00:00:01.000"); // 1秒後
        final Timestamp reloadTime = new Timestamp("2011/09/17 00:00:02.000"); // 2秒後

        context.checking(new Expectations() {
            {
                exactly(3).of(mockClock).getCurrentTime();
                will(onConsecutiveCalls(returnValue(loadTime), returnValue(fetchTime), returnValue(reloadTime)));

                exactly(2).of(mockLoader).load("KEY");
                will(onConsecutiveCalls(returnValue("VALUE"), returnValue("NEW-VALUE")));

                atLeast(1).of(mockPolicy).shouldReload(loadTime, fetchTime);
                will(returnValue(true));
            }
        });

        TimedCache cache = new TimedCache(mockLoader, mockClock, mockPolicy);
        assertThat("ロードされたオブジェクト", (String) cache.lookup("KEY"), is("VALUE"));
        assertThat("キャッシュされたオブジェクト", (String) cache.lookup("KEY"), is("NEW-VALUE"));
    }

}
