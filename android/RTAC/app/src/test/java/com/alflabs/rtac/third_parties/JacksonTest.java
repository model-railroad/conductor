package com.alflabs.rtac.third_parties;

import com.alflabs.rtac.BuildConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class JacksonTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    public static class Data {
        public int mFieldA;
        public String mFieldB;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Data)) return false;

            Data data = (Data) o;

            if (mFieldA != data.mFieldA) return false;
            return mFieldB != null ? mFieldB.equals(data.mFieldB) : data.mFieldB == null;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "mFieldA=" + mFieldA +
                    ", mFieldB='" + mFieldB + '\'' +
                    '}';
        }
    }

    @Before
    public void setUp() throws Exception {
        // no-op?
    }

    @Test
    public void testJackson() throws Exception {
        Data d1 = new Data();
        d1.mFieldA = 42;
        d1.mFieldB = "Answer";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // hide errors for prod
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(d1);
        assertThat(json).isNotEmpty();

        Data d2 = mapper.readValue(json, Data.class);
        assertThat(d2).isNotNull();
        assertThat(d2).isEqualTo(d1);
        assertThat(d2).isNotSameAs(d1);
    }
}
