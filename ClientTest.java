import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;

class ClientTest {

	@Test
	void test() {
		Image newImage = Client.toImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
		BufferedImage bImage = Client.toBufferedImage(newImage);
		assertTrue(Client.toBufferedImage(newImage) instanceof BufferedImage);
		assertTrue(Client.toImage(bImage) instanceof Image);
	}

}
