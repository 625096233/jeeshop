package org.rembx.jeeshop.mail;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.*;

import javax.mail.MessagingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class MailerIT {

    protected static Weld weld;
    protected static WeldContainer container;

    private SimpleSmtpServer server;

    @BeforeClass
    public static void init() {
        weld = new Weld();
        container = weld.initialize();
    }

    @AfterClass
    public static void close() {
        weld.shutdown();
    }

    @Before
    public void setup() {
        server = SimpleSmtpServer.start(2525);
    }

    @After
    public void after(){
        server.stop();
    }

    @Test
    public void sendMail() {
        Mailer mailer = container.instance().select(Mailer.class).get();
        try {
            mailer.sendMail("Test Subject", "test@test.com", "<h1>Hello</h1>");
        } catch (MessagingException e) {
            e.printStackTrace();
            fail();
        }

        assertThat(server.getReceivedEmailSize()).isEqualTo(1);
        assertThat(((SmtpMessage) server.getReceivedEmail().next()).getBody()).contains("<h1>Hello</h1>");
        assertThat(((SmtpMessage) server.getReceivedEmail().next()).getHeaderValue("Subject")).isEqualTo("Test Subject");
    }

    @Test
    public void sendMail_shouldThrowConnectTimeoutEx_WhenNoSmtpServerAvailable(){
        server.stop();
        Mailer mailer = container.instance().select(Mailer.class).get();
        try {
            mailer.sendMail("Test Subject", "test@test.com", "<h1>Hello</h1>");
            fail("should have thrown ex");
        }catch (MessagingException e){
            assertThat(e.getMessage()).startsWith("Couldn't connect to host");
        }
    }

}
