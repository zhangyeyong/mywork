import java.io.File;

import com.zte.mcore.ioc.BeanContext;
import com.zte.mcore.utils.McoreU;

public class Timer {

    public static void main(String[] args) {
        McoreU.setHomeFolder(new File("d:/mcore"));
        BeanContext ctx = new BeanContext();
        ctx.addScanPackage("com.zte.mcore.timer");
        ctx.startup();
    }

}
