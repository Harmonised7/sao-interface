package harmonised.saoui.config;

import harmonised.saoui.network.MessageConfefeg;
import harmonised.saoui.network.NetworkHandler;
import harmonised.saoui.util.Reference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Confefeger
{
    //Static Fields
    public static final Logger LOGGER = LogManager.getLogger();

    public static final Map<String, Confefeger> confefegers = new HashMap<>();

    //Confefeger Fields
    public final String confefegName;

    private Map<String, Confefeg> confefegs = new HashMap<>();
    private Map<String, String> parsedConfefeg = new HashMap<>();

    //Confefeger Methods
    private Confefeger( String confefeName )
    {
        this.confefegName = confefeName;
        parseConfefegs();
    }

    public ConfefeBuilder build( String name )
    {
        return new Confefeger.ConfefeBuilder( this, name );
    }

    public void saveConfefegs()
    {
        String tomlConfig = getConfefegsAsToml();

        File configFile = getConfigFile();
        try
        {
            configFile.getParentFile().mkdir();
            configFile.createNewFile();
        }
        catch( IOException e )
        {
            LOGGER.error( "Could not save " + Reference.MOD_ID + " Config!", configFile.getPath(), e );
        }

        try( FileOutputStream outputStream = new FileOutputStream( configFile ) )
        {
            System.out.println( "Writing " + Reference.MOD_ID + " Config to " + configFile.getPath() );
            IOUtils.write( tomlConfig, outputStream );
        }
        catch( IOException e )
        {
            LOGGER.error( "Error writing " + Reference.MOD_ID + " Config file to " + configFile.getPath(), configFile.getPath(), e );
        }
    }

    public void parseConfefegs()
    {
        parsedConfefeg.clear();
        File configFile = getConfigFile();
        if( !configFile.exists() )
            saveConfefegs();
        try
        (
            FileInputStream inputStream = new FileInputStream( configFile );
            InputStreamReader inputStreamReader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 );
            BufferedReader reader = new BufferedReader( inputStreamReader );
        )
        {
            LOGGER.debug( "Reading " + configFile.getName(), configFile.getPath() );
            for( String line : reader.lines().collect( Collectors.toList() ) )
            {
                if( line.length() == 0 || line.charAt(0) == '#' )
                    continue;
                int equalsIndex = line.indexOf( '=' );
                if( equalsIndex != -1 )
                {
                    String key = line.substring( 0, equalsIndex );
                    String stringValue = line.substring( equalsIndex+1 );
                    parsedConfefeg.put( key, stringValue );
                }
            }
        }
        catch( IOException e )
        {
            LOGGER.error( "Error parsing Confefeg: " + configFile.getPath(), e );
        }
    }

    public void syncConfefegs( ServerPlayerEntity player )
    {
        for( Confefeger.Confefeg confefeg : getConfefegs().values() )
        {
            NetworkHandler.sendToPlayer( new MessageConfefeg( Confefeger.confefegToNBT( confefeg ) ), player );
        }
    }

    public String getConfefegsAsToml()
    {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<Confefeg>> categories = new HashMap<>();
        for( Confefeg confefeg : confefegs.values() )
        {
            String categoryName = confefeg.category;
            if( !categories.containsKey( categoryName ) )
                categories.put( categoryName, new ArrayList<>() );
            categories.get( categoryName ).add( confefeg );
        }
        for( Map.Entry<String, List<Confefeg>> entry : categories.entrySet() )
        {
            stringBuilder.append( "###" + entry.getKey() + "###\n" );
            for( Confefeg confefeg : entry.getValue() )
            {
                stringBuilder.append( generateConfefegString( confefeg ) + "\n" );
            }
        }
        return stringBuilder.toString();
    }

    public File getConfigFile()
    {
        return FMLPaths.CONFIGDIR.get().resolve( confefegName + ".toml" ).toFile();
    }

    public void loadConfefeg( Confefeg confefeg )
    {
        String parsedValueString = confefeg.confefeger.parsedConfefeg.get( confefeg.name );
        if( parsedValueString != null )
        {
            try
            {
                Object value = confefeg.value;
                if( value instanceof String )
                    confefeg.set( value );
                else
                {
                    if( value instanceof Integer )
                        confefeg.set( Math.max( (int) confefeg.min, Math.min( (int) confefeg.max, Integer.parseInt( parsedValueString ) ) ) );
                    else if( value instanceof Float )
                        confefeg.set( Math.max( (float) confefeg.min, Math.min( (float) confefeg.max, Float.parseFloat( parsedValueString ) ) ) );
                    else if( value instanceof Double )
                        confefeg.set( Math.max( (double) confefeg.min, Math.min( (double) confefeg.max, Double.parseDouble( parsedValueString ) ) ) );
                }
            }
            catch( Exception e )
            {
                LOGGER.warn( "Invalid \""+ confefeg.name + "\" Confefe \"" + parsedValueString + "\"", e );
            }
        }
        LOGGER.info( "Loaded Confefeg \"" + confefeg.name + "\" as " + confefeg.value );
    }

    public Confefeg getConfefeg( String confefegName )
    {
        return confefegs.getOrDefault( confefegName, null );
    }

    public Map<String, Confefeg> getConfefegs()
    {
        return confefegs;
    }

    public void reloadConfefegs()
    {
        parseConfefegs();
        for( Confefeg confefeg : confefegs.values() )
        {
            loadConfefeg( confefeg );
        }
    }

    //Static Methods
    public static void syncAllConfefegs( ServerPlayerEntity player )
    {
        for( Confefeger confefeger : confefegers.values() )
        {
            confefeger.syncConfefegs( player );
        }
    }

    public static void saveAllConfefegers()
    {
        for( Confefeger confefeger : confefegers.values() )
        {
            confefeger.parseConfefegs();
            confefeger.saveConfefegs();
        }
    }

    public static void reloadAllConfefegs()
    {
        for( Confefeger confefeger : confefegers.values() )
        {
            confefeger.parseConfefegs();
            confefeger.reloadConfefegs();
        }
    }

    public static Confefeger registerConfefeg( String confefeName )
    {
        Confefeger confefeger = new Confefeger( confefeName );
        confefegers.put( confefeName, confefeger );
        return confefeger;
    }

    public static String generateConfefegString( Confefeg confefeg )
    {
        String output = "";

        output += "#Description:\t" + confefeg.description + "\n";
        if( !( confefeg.value instanceof String ) )
        output += "#Range:\t" + confefeg.min + "\tto\t" + confefeg.max + "\n";
        output += confefeg.name + "=" + confefeg.localValue + "\n";

        return output;
    }

    public static CompoundNBT confefegToNBT( Confefeg confefeg )
    {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putString( "key", confefeg.confefeger.confefegName );
        nbt.putString( "name", confefeg.name );
        if( confefeg.value instanceof String )
            nbt.putString( "value", (String) confefeg.value );
        else if( confefeg.value instanceof Integer )
            nbt.putDouble( "value", (int) confefeg.value );
        else if( confefeg.value instanceof Float )
            nbt.putDouble( "value", (float) confefeg.value );
        else if( confefeg.value instanceof Double )
            nbt.putDouble( "value", (double) confefeg.value );

        return nbt;
    }

    //Others
    public enum Side
    {
        LOCAL,
        COMMON
    }

    public static class ConfefeBuilder
    {
        private final Confefeger confefeger;
        private final String name;
        private Side side = Side.COMMON;
        private String description = "No Description", category = "General";

        public ConfefeBuilder( Confefeger confefeger, String name )
        {
            this.confefeger = confefeger;
            this.name = name;
        }

        public ConfefeBuilder description( String description )
        {
            this.description = description;
            return this;
        }

        public ConfefeBuilder category( String category )
        {
            this.category = category;
            return this;
        }

        public ConfefeBuilder side( Side side )
        {
            this.side = side;
            return this;
        }

        public Confefeg<Double> submit( double value, double min, double max )
        {
            return new Confefeg<>( confefeger, name, description, category, side, value, min, max );
        }

        public Confefeg<Float> submit( float value, float min, float max )
        {
            return new Confefeg<>( confefeger, name, description, category, side, value, min, max );
        }

//        public Confefeg<Long> submit( long value, long min, long max )
//        {
//            return new Confefeg<>( confefeger, name, description, category, side, value, min, max );
//        }

        public Confefeg<Integer> submit( int value, int min, int max )
        {
            return new Confefeg<>( confefeger, name, description, category, side, value, min, max );
        }

//        public Confefeg<Character> submit( char value )
//        {
//            Confefeg<Character> confefeg = new Confefeg<>( confefeger, name, description, category, side, value );
//            addConfefeg( confefeg );
//            return confefeg;
//        }

        public Confefeg<String> submit( String value )
        {
            return new Confefeg<>( confefeger, name, description, category, side, value );
        }
    }

    public static class Confefeg<T> implements Supplier<T>
    {
        public final Confefeger confefeger;
        public final String name, description, category;
        public final Side side;
        private T value, localValue, min, max;

        public Confefeg( Confefeger confefeger, String name, String description, String category, Side side, T value, T min, T max )
        {
            this.confefeger = confefeger;
            this.name = name;
            this.description = description;
            this.category = category;
            this.side = side;
            this.value = value;
            this.localValue = value;
            this.min = min;
            this.max = max;
            confefeger.confefegs.put( name, this );
            confefeger.loadConfefeg( this );
        }

        public Confefeg( Confefeger confefeger, String name, String description, String category, Side side, T value )
        {
            this.confefeger = confefeger;
            this.name = name;
            this.description = description;
            this.category = category;
            this.side = side;
            this.value = value;
            this.localValue = value;
            this.min = value;
            this.max = value;
            confefeger.confefegs.put( name, this );
        }

        public T getMin()
        {
            return min;
        }

        public T getMax()
        {
            return max;
        }

        @Override
        public T get()
        {
            return value;
        }

        public T getLocal()
        {
            return localValue;
        }
        
        public void set( T value )
        {
            this.value = value;
            this.localValue = value;
//            if( this.side == Side.COMMON )
//            {
                //Send packet to set config from to either side?
//            }
        }

        /**
         * This should only be used from a packet received on client.
         * This method sets the value, without disturbing the localValue.
         * @param value
         */
        @Deprecated
        public void setFromServer( T value )
        {
            this.value = value;
        }
    }
}
