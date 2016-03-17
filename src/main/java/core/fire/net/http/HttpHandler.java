/**
 * 
 */
package core.fire.net.http;

import java.util.List;
import java.util.Map;

import io.netty.channel.Channel;

/**
 * @author lhl
 *
 *         2016年3月16日 下午4:26:27
 */
public interface HttpHandler
{
    void handle(Channel ch, String uri, Map<String, List<String>> parameter);
}