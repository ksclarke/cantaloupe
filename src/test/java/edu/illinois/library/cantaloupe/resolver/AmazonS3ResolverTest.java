package edu.illinois.library.cantaloupe.resolver;

import edu.illinois.library.cantaloupe.Application;
import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.image.SourceFormat;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * <p>Tests AmazonS3Resolver against Amazon S3 -- the actual AWS S3, not a
 * mock, which has pros and cons (pro: guaranteed accuracy; con: need an AWS
 * account).</p>
 *
 * <p>This test requires a file to be present at {user.home}/.s3/cantaloupe
 * that is formatted as such:</p>
 *
 * <pre>AWSAccessKeyId=xxxxxx
 * AWSSecretKey=xxxxxx
 * Bucket=xxxxxx</pre>
 *
 * <p>Also, the bucket must contain an image called f50.jpg.</p>
 */
public class AmazonS3ResolverTest {

    private static final Identifier IMAGE = new Identifier("f50.jpg");
    AmazonS3Resolver instance;

    @Before
    public void setUp() throws IOException {
        FileInputStream fis = new FileInputStream(new File(
                System.getProperty("user.home") + "/.s3/cantaloupe"));
        String authInfo = IOUtils.toString(fis);
        String[] lines = StringUtils.split(authInfo, "\n");
        final String accessKeyId = lines[0].replace("AWSAccessKeyId=", "").trim();
        final String secretKey = lines[1].replace("AWSSecretKey=", "").trim();
        final String bucket = lines[2].replace("Bucket=", "").trim();

        BaseConfiguration config = new BaseConfiguration();
        config.setProperty(AmazonS3Resolver.BUCKET_NAME_CONFIG_KEY, bucket);
        config.setProperty(AmazonS3Resolver.ACCESS_KEY_ID_CONFIG_KEY, accessKeyId);
        config.setProperty(AmazonS3Resolver.SECRET_KEY_CONFIG_KEY, secretKey);
        // For future reference, note that we can also set
        // AmazonS3Resolver.ENDPOINT_CONFIG_KEY to point it at a mocked S3
        // instance.
        Application.setConfiguration(config);

        instance = new AmazonS3Resolver();
    }

    @Test
    public void testGetChannel() {
        // present, readable image
        try {
            assertNotNull(instance.getChannel(IMAGE));
        } catch (IOException e) {
            fail();
        }
        // missing image
        try {
            instance.getChannel(new Identifier("bogus"));
            fail("Expected exception");
        } catch (FileNotFoundException e) {
            // pass
        } catch (IOException e) {
            fail("Expected FileNotFoundException");
        }
        // present, unreadable image
        // TODO: write this
    }

    @Test
    public void testGetSourceFormat() throws IOException {
        assertEquals(SourceFormat.JPG, instance.getSourceFormat(IMAGE));
        try {
            instance.getSourceFormat(new Identifier("image.bogus"));
            fail("Expected exception");
        } catch (IOException e) {
            // pass
        }
        try {
            instance.getSourceFormat(new Identifier("image"));
            fail("Expected exception");
        } catch (IOException e) {
            // pass
        }
    }

}
