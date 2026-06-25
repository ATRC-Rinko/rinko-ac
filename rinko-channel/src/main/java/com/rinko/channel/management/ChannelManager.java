package com.rinko.channel.management;

import com.rinko.channel.bot.BotContext;
import com.rinko.channel.bot.ChannelBot;
import com.rinko.channel.model.vo.ChannelStatusVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    private static final Logger log = LoggerFactory.getLogger(ChannelManager.class);

    private final Map<String, ChannelBot> bots = new ConcurrentHashMap<>();
    private final Map<String, BotContext> botContexts = new ConcurrentHashMap<>();

    public void register(String botId, ChannelBot bot, BotContext context) {
        bots.put(botId, bot);
        botContexts.put(botId, context);
        log.info("Registered bot: {} ({})", botId, bot.getPlatform());
    }

    public void register(String botId, ChannelBot bot) {
        register(botId, bot, null);
    }

    public void unregister(String botId) {
        var bot = bots.remove(botId);
        botContexts.remove(botId);
        if (bot != null) {
            bot.stop();
        }
    }

    public void startBot(String botId) {
        var bot = bots.get(botId);
        var ctx = botContexts.get(botId);
        if (bot != null && ctx != null) {
            bot.start(ctx);
        }
    }

    public void stopBot(String botId) {
        var bot = bots.get(botId);
        if (bot != null) {
            bot.stop();
        }
    }

    public List<ChannelStatusVO> listBots() {
        return bots.values().stream()
            .map(b -> {
                var s = b.getStatus();
                return new ChannelStatusVO(
                    s.platform(), s.botId(), s.state().name(), s.detail(), 0);
            })
            .toList();
    }

    public ChannelStatusVO getBotStatus(String botId) {
        var bot = bots.get(botId);
        if (bot == null) return null;
        var s = bot.getStatus();
        return new ChannelStatusVO(
            s.platform(), s.botId(), s.state().name(), s.detail(), 0);
    }
}
