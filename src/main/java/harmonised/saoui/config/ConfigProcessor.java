package harmonised.saoui.config;

import harmonised.saoui.util.Reference;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigProcessor
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static Map<String, Map<Field, Object>> defaultValues = new HashMap<>();
    private static final String configPath = "saoui.toml";
    private static boolean defaultsStored = false;

    public static void readConfig( Class configClass )
    {
        storeDefaultValues( configClass );
        parseConfig( configClass );
        for( Field field : configClass.getDeclaredFields() )
        {
            readConfigField( field );
        }
    }

    public static void readConfigField( Field field )
    {
        if( field.isAnnotationPresent( Config.Name.class ) )
        {
            Type type = field.getGenericType();
            if( type == int.class )
            {
                Integer readInValue = readOrGenerateIntConfig( field );
                int value = (int) readField( field );
                if( readInValue != null )
                    value = readInValue;
                if( field.isAnnotationPresent( Config.RangeInt.class ) )
                    value = limitInt( value, field.getAnnotation( Config.RangeInt.class ) );
                setInt( field, value );
            }
            else if( type == double.class || type == float.class )
            {
                boolean isFloat = type == float.class;
                Double readInValue = readOrGenerateDoubleConfig( field );
                double value;
                if( isFloat )
                    value = (float) readField( field );
                else
                    value = (double) readField( field );
                if( readInValue != null )
                    value = readInValue;
                if( field.isAnnotationPresent( Config.RangeDouble.class ) )
                    value = limitDouble( value, field.getAnnotation( Config.RangeDouble.class ) );
                if( isFloat )
                    setFloat( field, (float) value );
                else
                    setDouble( field, value );
            }
        }
    }

    private static void parseConfig( Class configClass )
    {
        File configFile = FMLPaths.CONFIGDIR.get().resolve(configPath).toFile();
        if( !configFile.exists() )
            saveConfig( configClass );
        Map<String, Field> fields = new HashMap<>();
        for( Field field : configClass.getDeclaredFields() )
        {
            if( field.isAnnotationPresent( Config.Name.class ) )
                fields.put( field.getName(), field );
        }

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
                    Field field = fields.get( key );
                    if( field != null )
                    {
                        String stringValue = line.substring( equalsIndex+1 );
                        Type type = field.getGenericType();
                        if( type == int.class )
                        {
                            try
                            {
                                int value = Integer.parseInt( stringValue );
                                setInt( field, value );
                            }
                            catch( Exception e )
                            {
                                LOGGER.error( "Error parsing " + field.getName() + " Config" );
                            }
                        }
                        else if( type == float.class || type == double.class )
                        {
                            try
                            {
                                double value = Double.parseDouble( stringValue );
                                if( type == float.class )
                                    setFloat( field, (float) value );
                                else
                                    setDouble( field, value );
                            }
                            catch( Exception e )
                            {
                                LOGGER.error( "Error parsing " + field.getName() + " Config", e );
                            }
                        }
                    }
                }
            }
        }
        catch( IOException e )
        {
            LOGGER.error( "Error copying over " + configFile.getName() + " json config to " + configFile.getPath(), configFile.getPath(), e );
        }
    }

    public static void saveConfig( Class configClass )
    {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<Field>> categories = new HashMap<>();
        for( Field field : configClass.getDeclaredFields() )
        {
            if( field.isAnnotationPresent( Config.Name.class ) )
            {
                if( field.isAnnotationPresent( Config.Category.class ) )
                {
                    String categoryName = field.getAnnotation( Config.Category.class ).value();
                    if( !categories.containsKey( categoryName ) )
                        categories.put( categoryName, new ArrayList<>() );
                    categories.get( categoryName ).add( field );
                }

            }
        }
        for( Map.Entry<String, List<Field>> entry : categories.entrySet() )
        {
            stringBuilder.append( "###" + entry.getKey() + "###\n" );
            for( Field field : entry.getValue() )
            {
                if( field.isAnnotationPresent( Config.Name.class ) )
                    stringBuilder.append( generateConfigString(field) + "\n" );
            }
        }
        String output = stringBuilder.toString();
        File configFile = FMLPaths.CONFIGDIR.get().resolve(configPath).toFile();
        try
        {
            configFile.getParentFile().mkdir();
            configFile.createNewFile();
        }
        catch( IOException e )
        {
            LOGGER.error( "Could save " + Reference.MOD_ID + " Config!", configFile.getPath(), e );
        }

        try( FileOutputStream outputStream = new FileOutputStream( configFile ) )
        {
            System.out.println( "Writing " + Reference.MOD_ID + " Config to " + configFile.getPath() );
            IOUtils.write( output, outputStream );
        }
        catch( IOException e )
        {
            LOGGER.error( "Error writing " + Reference.MOD_ID + " Config file to " + configFile.getPath(), configFile.getPath(), e );
        }
    }

    public static void storeDefaultValues( Class configClass )
    {
        if( !defaultsStored )
        {
            for( Field field : configClass.getDeclaredFields() )
            {
                if( field.isAnnotationPresent( Config.Name.class ) )
                {
                    if( !defaultValues.containsKey( configClass.getName() ) )
                        defaultValues.put( configClass.getName(), new HashMap<>() );
                    defaultValues.get( configClass.getName() ).put( field, readField( field ) );
                }
            }
            defaultsStored = true;
        }
    }

    public static String generateConfigString( Field field )
    {
        Type type = field.getGenericType();
        String output = "";

        if( field.isAnnotationPresent( Config.Name.class ) )
            output += "#Name\t" + field.getAnnotation( Config.Name.class ).value() + "\n";
        if( field.isAnnotationPresent( Config.Comment.class ) )
            output += "#Comment\t" + field.getAnnotation( Config.Comment.class ).value() + "\n";
        if( field.isAnnotationPresent( Config.RangeInt.class ) )
        {
            Config.RangeInt rangeInt = field.getAnnotation( Config.RangeInt.class );
            output += "#Range\t" + rangeInt.min() + "\tto\t" + rangeInt.max() + "\n";
        }
        else if( field.isAnnotationPresent( Config.RangeDouble.class ) )
        {
            Config.RangeDouble rangeDouble = field.getAnnotation( Config.RangeDouble.class );
            output += "#Range\t" + rangeDouble.min() + "\tto\t" + rangeDouble.max() + "\n";
        }
        output += field.getName() + "=" + readField( field ) + "\n";

        return output;
    }

    public static Object getDefaultFieldValue( Field field )
    {
        return defaultValues.get( field );
    }

    public static void setInt( Field field, int value )
    {
        try
        {
            field.setInt( field, value );
        }
        catch( IllegalAccessException e )
        {
            LOGGER.error( "Could write to field " + field.getName(), e );
        }
    }

    public static void setFloat( Field field, float value )
    {
        try
        {
            field.setFloat( field, value );
        }
        catch( IllegalAccessException e )
        {
            LOGGER.error( "Could write to field " + field.getName(), e );
        }
    }

    public static void setDouble( Field field, double value )
    {
        try
        {
            field.setDouble( field, value );
        }
        catch( IllegalAccessException e )
        {
            LOGGER.error( "Could write to field " + field.getName(), e );
        }
    }

    public static Object readField( Field field )
    {
        try
        {
            return field.get( field );
        }
        catch( IllegalAccessException e )
        {
            LOGGER.error( "Could read from field " + field.getName() );
            return null;
        }
    }

    public static int limitInt( int input, Config.RangeInt rangeDouble )
    {
        return Math.max( rangeDouble.min(), Math.min( rangeDouble.max(), input ) );
    }

    public static double limitDouble( double input, Config.RangeDouble rangeDouble )
    {
        return Math.max( rangeDouble.min(), Math.min( rangeDouble.max(), input ) );
    }

    public static Integer readOrGenerateIntConfig( Field field )
    {
        return (int) Math.floor( readOrGenerateDoubleConfig( field ) );
    }

    public static Float readOrGenerateFloatConfig( Field field )
    {
        return (float) ( readOrGenerateDoubleConfig( field ) + 0f );
    }

    public static Double readOrGenerateDoubleConfig( Field field )
    {
        return 5D;
    }
}