package net.md_5.bungee;


import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.command.ConsoleCommandSender;


public class BungeeCordLauncher
{
    @SuppressWarnings("checkstyle:ParenPad")
    public static void main(String[] args) throws Exception
    {
        Security.setProperty( "networkaddress.cache.ttl", "30" );
        Security.setProperty( "networkaddress.cache.negative.ttl", "10" );
        // For JDK9+ we force-enable multi-release jar file support #3087
        if ( System.getProperty( "jdk.util.jar.enableMultiRelease" ) == null )
        {
            System.setProperty( "jdk.util.jar.enableMultiRelease", "force" );
        }

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.acceptsAll( Arrays.asList( "help" ), "Show the help" );
        parser.acceptsAll( Arrays.asList( "v", "version" ), "Print version and exit" );
        parser.acceptsAll( Arrays.asList( "noconsole" ), "Disable console input" );

        OptionSet options = parser.parse( args );

        if ( options.has( "help" ) )
        {
            parser.printHelpOn( System.out );
            return;
        }
        if ( options.has( "version" ) )
        {
            System.out.println( BungeeCord.class.getPackage().getImplementationVersion() );
            return;
        }

        if ( BungeeCord.class.getPackage().getSpecificationVersion() != null && System.getProperty( "IReallyKnowWhatIAmDoingISwear" ) == null )
        {
            String version = BungeeCord.class.getPackage().getSpecificationVersion();

            if ( version.equalsIgnoreCase( "unknown" ) )
            {
                System.err.println( "*** You are using a self compiled version ***" );
                System.err.println( "*** Please make sure your server is up to date ***" );
                System.err.println( "*** Using current version without warranty ***" );
                System.err.println( "*** Server will start in 2 seconds ***" );
                Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
            } else
            {
                int currentVersion = Integer.parseInt( version );

                try
                {
                    URL api = new URL( "https://api.github.com/repos/SimplyRin/HexaCord/releases/latest" );
                    URLConnection con = api.openConnection();
                    // 15 second timeout at various stages
                    con.setConnectTimeout( 15000 );
                    con.setReadTimeout( 15000 );

                    String tagName = null;

                    try
                    {
                        JsonObject json = new JsonParser().parse( new InputStreamReader( con.getInputStream() ) ).getAsJsonObject();
                        tagName = json.get( "tag_name" ).getAsString();

                        int latestVersion = Integer.parseInt( tagName.substring( 1, tagName.length() ) );

                        if ( latestVersion > currentVersion )
                        {
                            System.err.println( "*** Warning, this build is outdated ***" );
                            System.err.println( "*** Please download a new build from https://ci.simplyrin.net/job/HexaCord ***" );
                            System.err.println( "*** You will get NO support regarding this build ***" );
                            System.err.println( "*** Server will start in 10 seconds ***" );
                            Thread.sleep( TimeUnit.SECONDS.toMillis( 10 ) );
                        }
                    } catch ( JsonIOException e )
                    {
                        throw new IOException( e );
                    } catch ( JsonSyntaxException e )
                    {
                        throw new IOException( e );
                    } catch ( NumberFormatException e )
                    {
                        throw new IOException( e );
                    }
                } catch ( IOException e )
                {
                    System.err.println( "*** Can not test if up to date ***" );
                    System.err.println( "*** Using current version without warranty ***" );
                    System.err.println( "*** Server will start in 2 seconds ***" );
                    Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
                }
            }
        }

        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        bungee.getLogger().info( "Enabled BungeeCord version " + bungee.getVersion() );
        bungee.start();

        if ( !options.has( "noconsole" ) )
        {
            String line;
            while ( bungee.isRunning && ( line = bungee.getConsoleReader().readLine( ">" ) ) != null )
            {
                if ( !bungee.getPluginManager().dispatchCommand( ConsoleCommandSender.getInstance(), line ) )
                {
                    bungee.getConsole().sendMessage( new ComponentBuilder( "Command not found" ).color( ChatColor.RED ).create() );
                }
            }
        }
    }
}
