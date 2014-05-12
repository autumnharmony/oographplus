import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CodegenLogging2 {

    @Pointcut("execution(public ru.ssau.graphplus.commons.ShapeHelperWrapper.getNodeType())")
    public void shapeHelperGetNodeType() {
        System.out.println("ASPECTJ!!!");
    }
}
