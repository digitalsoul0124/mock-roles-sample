package time;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TimedCacheTest {

    private Mockery context = new Mockery();

    @Test
    public void キャッシュされていないオブジェクトはロードする() throws Exception {

        final ObjectLoader mockLoader = context.mock(ObjectLoader.class);

        context.checking(new Expectations() {
            {
                oneOf(mockLoader).load("KEY1");
                will(returnValue("VALUE1"));
            }
        });

        TimedCache cache = new TimedCache(mockLoader);
        assertThat((String) cache.lookup("KEY1"), is("VALUE1"));
    }

    @Test
    public void キャッシュされたオブジェクトはロードしない() throws Exception {

        final ObjectLoader mockLoader = context.mock(ObjectLoader.class);

        context.checking(new Expectations() {
            {
                oneOf(mockLoader).load("KEY1");
                will(returnValue("VALUE1"));
            }
        });

        TimedCache cache = new TimedCache(mockLoader);
        assertThat("ロードされたオブジェクト", (String) cache.lookup("KEY1"), is("VALUE1"));
        assertThat("キャッシュされたオブジェクト", (String) cache.lookup("KEY1"), is("VALUE1"));
    }

}
