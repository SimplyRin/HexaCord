package net.md_5.bungee.module.cmd.bungee;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{
    private PluginBungee plugin;

    public CommandBungee(PluginBungee plugin)
    {
        super( "bungee", "bungeecord.command.bungee" );

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        TextComponent bungee = new TextComponent( ChatColor.BLUE + "This server is running HexaCord version " + ProxyServer.getInstance().getVersion() + " by md_5" );
        bungee.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( "Click to copy to clipboard" ) ) );
        bungee.setClickEvent( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, ChatColor.stripColor( bungee.getText() ) ) );

        sender.sendMessage( bungee );

        TextComponent protocol = new TextComponent( ChatColor.BLUE + "Protocol support for 1.7.x by Zartec, ghac and I9hdkill" );
        protocol.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( "Click to copy to clipboard" ) ) );
        protocol.setClickEvent( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, ChatColor.stripColor( protocol.getText() ) ) );

        sender.sendMessage( protocol );

        sender.sendMessage( "Checking version. please wait..." );

        String version = ProxyServer.getInstance().getVersion();
        int lastColon = version.lastIndexOf( ':' );
        String buildNumber = version.substring( lastColon + 1, version.length() );

        if ( "unknown".equals( buildNumber ) )
        {
            sender.sendMessage( "Couldn't detect bungee version. Custom build?" );
            return;
        }

        int currentBuildNumber = Integer.valueOf( buildNumber ).intValue();

        ProxyServer.getInstance().getScheduler().runAsync( this.plugin, () ->
        {
            try
            {
                HttpsURLConnection connection = (HttpsURLConnection) new URL( "https://ci.simplyrin.net/job/HexaCord/lastStableBuild/buildNumber" ).openConnection();
                connection.addRequestProperty( "user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36" );

                // 5 second timeout at various stages
                connection.setConnectTimeout( 5000 );
                connection.setReadTimeout( 5000 );

                String result = new BufferedReader( new InputStreamReader( connection.getInputStream(), StandardCharsets.UTF_8 ) ).lines().collect( Collectors.joining() );

                int lastStableBuild = Integer.valueOf( result ).intValue();

                if ( lastStableBuild == currentBuildNumber )
                {
                    TextComponent latest = new TextComponent( ChatColor.GREEN + "You are running the latest version" );
                    latest.setHoverEvent( bungee.getHoverEvent() );
                    latest.setClickEvent( bungee.getClickEvent() );

                    sender.sendMessage( latest );
                } else
                {
                    TextComponent behind = new TextComponent( ChatColor.YELLOW + String.format( "You are %d version(s) behind", lastStableBuild - currentBuildNumber ) );
                    behind.setHoverEvent( bungee.getHoverEvent() );
                    behind.setClickEvent( bungee.getClickEvent() );

                    sender.sendMessage( behind );

                    TextComponent text = new TextComponent( ChatColor.YELLOW + "Download the new version at: " );
                    text.setHoverEvent( bungee.getHoverEvent() );
                    text.setClickEvent( bungee.getClickEvent() );

                    TextComponent url = new TextComponent( ChatColor.GOLD + "https://ci.simplyrin.net/job/HexaCord/" );
                    url.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( "Click to open" ) ) );
                    url.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://ci.simplyrin.net/job/HexaCord/" ) );

                    text.addExtra( url );

                    sender.sendMessage( text );
                }
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        } );
    }
}
