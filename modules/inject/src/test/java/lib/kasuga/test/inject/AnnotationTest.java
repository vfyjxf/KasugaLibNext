package lib.kasuga.test.inject;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanResolutionTraceMode;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.inject.qualifiers.Qualifiers;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class AnnotationTest {
    private ApplicationContext context = ApplicationContext.builder().beanResolutionTrace(BeanResolutionTraceMode.STANDARD_OUT).start();

    @Test
    public void shouldBeanScanningWorks() {
        Assertions.assertDoesNotThrow(()->{
            TestBean testBean = context.getBean(TestBean.class, Qualifiers.byName("mustInject"));
            Assertions.assertNotNull(testBean);
        });

        Assertions.assertThrows(NoSuchBeanException.class, ()->{
            TestBean testBean = context.getBean(TestBean.class, Qualifiers.byName("mustNotInject"));
        });
    }

    @Test
    public void shouldNotInjectBeanClassNotLoaded(){
        Assertions.assertFalse(TestFlags.notInjectBeanLoaded);
    }
}
