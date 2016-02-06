package bio.comp.orion.model;
import bio.comp.orion.model.Colors;
import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.Color;

public class ColorsTest {
    @Test
    public void createsColorFromHex(){
        Color bluish = Colors.fromHexString("#0033FF");
        assertNotNull( "color should be created with valid code", bluish);
    }

}
