package harmonised.annotfig.config;

import java.util.HashMap;

public class Configs
{
    private static final HashMap<String, ConfigEntry> configs = new HashMap<>();

    public static void register( String configKey, Class configClass )
    {
        configs.put( configKey, new ConfigEntry( configKey, configClass ) );
    }

    public static boolean parseConfig( String configKey )
    {
        if( configs.containsKey( configKey ) )
        {
            configs.get( configKey ).parseConfigs();
//            configs.get( configKey ).saveConfig( true );
            return true;
        }
        else
            return false;
    }

    public static boolean saveConfig( String configKey )
    {
        if( configs.containsKey( configKey ) )
        {
            configs.get( configKey ).saveConfig( false );
//            configs.get( configKey ).saveConfig( true );
            return true;
        }
        else
            return false;
    }
}
