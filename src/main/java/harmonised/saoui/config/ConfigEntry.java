package harmonised.annotfig.config;

import harmonised.annotfig.util.Reference;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
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

public class ConfigEntry
{
    public final Logger LOGGER = LogManager.getLogger();

    final String cKey;
    final Class cClass;

    private static Map<String, Class> configs = new HashMap<>();
    private static Map<String, Map<Field, Object>> defaultValues = new HashMap<>();
    private static Map<String, Map<Field, Object>> localCommonConfig = new HashMap<>();

    public ConfigEntry( String configKey, Class configClass )
    {
        this.cKey = configKey;
        this.cClass = configClass;

        storeDefaultValues();
        parseConfigs();
        storeLocalValues();
    }

    public String getFieldsAsToml( Collection<Field> fields )
    {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<Field>> categories = new HashMap<>();
        for( Field field : fields )
        {
            if( isFieldConfig( field ) )
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
                if( isFieldConfig( field ) )
                    stringBuilder.append( generateConfigString( field ) + "\n" );
            }
        }
        return stringBuilder.toString();

    }

    public Map<String, Field> getFields()
    {
        Map<String, Field> fields = new HashMap<>();
        for( Field field : cClass.getDeclaredFields() )
        {
            if( isFieldConfig( field ) )
                fields.put( field.getName(), field );
        }
        return fields;
    }

    public Map<String, Field> getFields( boolean server )
    {
        Map<String, Field> fields = new HashMap<>();
        for( Field field : cClass.getDeclaredFields() )
        {
            if( isFieldConfig( field ) )
            {
                if( ( isFieldServer( field ) && server ) || ( !isFieldServer( field ) && !server ) )
                    fields.put( field.getName(), field );
            }
        }
        return fields;
    }

    public void saveConfig( boolean server )
    {
        Map<String, Field> fields = getFields( server );
        String tomlConfig = getFieldsAsToml( fields.values() );

        File configFile = getConfigFile( server );
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
            IOUtils.write( tomlConfig, outputStream );
        }
        catch( IOException e )
        {
            LOGGER.error( "Error writing " + Reference.MOD_ID + " Config file to " + configFile.getPath(), configFile.getPath(), e );
        }
    }

    public File getConfigFile( boolean server )
    {
        //TODO
        if( server )    //Return path to server
            return FMLPaths.CONFIGDIR.get().resolve( cKey + ".toml" ).toFile();
        else
            return FMLPaths.CONFIGDIR.get().resolve( cKey + ".toml" ).toFile();
    }

    public void parseConfigs()
    {
        parseConfig( false );
//        parseConfig( true );
    }

    public void parseConfig( boolean server )
    {
        File configFile = getConfigFile( server );
        if( !configFile.exists() )
            saveConfig( server );
        Map<String, Field> fields = getFields( server );

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
                                if( field.isAnnotationPresent( Config.RangeInt.class ) )
                                    value = limitInt( value, field.getAnnotation( Config.RangeInt.class ) );
                                setInt( field, value );
                            }
                            catch( Exception e )
                            {
                                LOGGER.error( "Error parsing " + field.getName() + " Config: \"" + stringValue + "\"" );
                            }
                        }
                        else if( type == float.class || type == double.class )
                        {
                            try
                            {
                                double value = Double.parseDouble( stringValue );
                                if( field.isAnnotationPresent( Config.RangeDouble.class ) )
                                    value = limitDouble( value, field.getAnnotation( Config.RangeDouble.class ) );
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

    private void storeDefaultValues()
    {
        
        if( !defaultValues.containsKey( cKey ) )
            defaultValues.put( cKey, new HashMap<>() );
        for( Field field : cClass.getDeclaredFields() )
        {
            if( isFieldConfig( field ) )
                defaultValues.get( cKey ).put( field, readField( field ) );
        }
    }

    private void storeLocalValues()
    {
        
        if( !localCommonConfig.containsKey( cKey ) )
            localCommonConfig.put( cKey, new HashMap<>() );
        for( Field field : cClass.getDeclaredFields() )
        {
            if( isFieldConfig( field ) && isFieldCommon( field ) )
                localCommonConfig.get( cKey ).put( field, readField( field ) );
        }
    }

    public boolean isFieldCommon( Field field )
    {
        return !field.isAnnotationPresent( Config.Type.class ) || ( field.isAnnotationPresent( Config.Type.class ) && field.getAnnotation( Config.Type.class ).value() == Config.ConfigType.COMMON );
    }

    public boolean isFieldServer( Field field )
    {
        return false;
//        return !field.isAnnotationPresent( Config.Type.class ) || ( field.isAnnotationPresent( Config.Type.class ) && field.getAnnotation( Config.Type.class ).value() == Config.ConfigType.SERVER );
    }

    public boolean isFieldConfig( Field field )
    {
        return field.isAnnotationPresent( Config.Name.class );
    }

    public String generateConfigString( Field field )
    {
        String output = "";

        if( isFieldConfig( field ) )
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

        Object fieldValue = isFieldCommon( field ) ? localCommonConfig.get( cKey ).get( field ) : readField( field );
        output += field.getName() + "=" + fieldValue + "\n";

        return output;
    }

    public Object getDefaultFieldValue( Field field )
    {
        return defaultValues.get( cKey ).get( field );
    }

    public void setInt( Field field, int value )
    {
        try
        {
            field.setInt( field, value );
        }
        catch( IllegalAccessException e )
        {
            LOGGER.error( "Couldn't write to field " + field.getName(), e );
        }
    }

    public void setFloat( Field field, float value )
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

    public void setDouble( Field field, double value )
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

    public Object readField( Field field )
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

    public int limitInt( int input, Config.RangeInt rangeDouble )
    {
        return Math.max( rangeDouble.min(), Math.min( rangeDouble.max(), input ) );
    }

    public double limitDouble( double input, Config.RangeDouble rangeDouble )
    {
        return Math.max( rangeDouble.min(), Math.min( rangeDouble.max(), input ) );
    }

    public Integer readOrGenerateIntConfig( Field field )
    {
        return (int) Math.floor( readOrGenerateDoubleConfig( field ) );
    }

    public Float readOrGenerateFloatConfig( Field field )
    {
        return (float) ( readOrGenerateDoubleConfig( field ) + 0f );
    }

    public Double readOrGenerateDoubleConfig( Field field )
    {
        return 5D;
    }
}