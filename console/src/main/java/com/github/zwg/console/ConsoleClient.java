package com.github.zwg.console;

import static jline.console.KeyMap.CTRL_D;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandParse;
import com.github.zwg.core.netty.ConnClient;
import com.github.zwg.core.netty.Constants;
import com.github.zwg.core.netty.MessageTypeEnum;
import com.github.zwg.core.netty.MessageUtil;
import io.netty.channel.Channel;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.UUID;
import jline.console.ConsoleReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/30
 */
public class ConsoleClient {

    private final ConsoleReader console;
    private final Channel channel;

    public ConsoleClient(InetSocketAddress inetSocketAddress) throws IOException {
        String sessionId = UUID.randomUUID().toString();
        this.console = initConsoleReader();
        PrintWriter out = new PrintWriter(console.getOutput());
        printBanner(out);
        ConnClient connClient = new ConnClient();
        channel = connClient
                .conn(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), out, sessionId);
        String line;
        while ((line = console.readLine()) != null) {
            // If we input the special word then we will mask
            // the next line.
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                shutdown();
                break;
            }
            if(!StringUtils.isBlank(line)){
                Command command = CommandParse.parse(line);
                if (command != null) {
                    channel.writeAndFlush(
                            MessageUtil.wrap(sessionId, MessageTypeEnum.REQUEST, null, command));
                } else {
                    out.println("please check if a bad command:" + line);
                }
            }else {
                out.write(Constants.PROMPT);
                out.flush();
            }
        }

    }

    private ConsoleReader initConsoleReader() throws IOException {
        ConsoleReader console = new ConsoleReader();
        //console.setPrompt(Constants.prompt);
        console.getKeys().bind("" + CTRL_D, (ActionListener) e -> shutdown());
        return console;
    }

    /**
     * 关闭Console
     */
    private void shutdown() {
        channel.close();
        console.shutdown();
    }

    private void printBanner(PrintWriter writer) {
        String blankSpace = "   ";
        try {
            String logo = IOUtils.toString(
                    ConsoleClient.class.getClassLoader().getResourceAsStream("banner.txt"));
            String version = IOUtils.toString(
                    ConsoleClient.class.getClassLoader().getResourceAsStream("version.txt"));
            writer.write(logo);
            writer.write(blankSpace + version);
            writer.println();
            writer.write(Constants.PROMPT);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException {
        new ConsoleClient(new InetSocketAddress("127.0.0.1", 8080));
    }
}
