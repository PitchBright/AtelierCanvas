package sidben.ateliercanvas.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import sidben.ateliercanvas.helper.LogHelper;
import sidben.ateliercanvas.reference.Reference;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;


public class ConfigurationHandler
{

    public static final String                   CATEGORY_PAINTINGS         = "paintings";
    public static final String                   CATEGORY_IMPORT            = "paintings_import";
    public static final String                   CATEGORY_EXPORT            = "paintings_export";
    public static final String                   PAINTINGS_ARRAY_KEY        = "painting_list";
    public final static String                   IMAGES_BASE_PATH           = "config/AtelierCanvas_Paintings/";
    


    /** List with info of all paintings installed */
    public static List<CustomPaintingConfigItem> mahPaintings;

    /** Largest file size (in bytes) accepted by the mod */
    public static long                           maxFileSize;

    /** Format in which the painting dates (created / last updated) will be displayed */
    public static String                         paintingDateFormat;

    /** Maximum painting size, in pixels, accepted by the mod. */
    public static int                            maxPaintingSize;

    /** Default resolution of paintings */
    public static final int                      defaultResolution          = 16;



    private static final int                     DEFAULT_maxFileSize        = 30;             // NOTE: the default file size is in KBytes to make the property slider more friendly
    private static final int                     DEFAULT_maxPaintingSize    = 64;
    private static final String                  DEFAULT_paintingDateFormat = "yyyy-MM-dd";



    // Instance
    public static Configuration                  config;



    public static void init(File configFile)
    {

        // Create configuration object from config file
        if (config == null) {
            config = new Configuration(configFile);
            loadConfig();
        }

    }



    private static void loadConfig()
    {
        final List<String> propOrder = new ArrayList<String>();
        Property prop;
        ConfigCategory cat;



        // Load properties
        prop = config.get(Configuration.CATEGORY_GENERAL, "max_filesize_kb", DEFAULT_maxFileSize, "", 10, 1024);     // 1048576 bytes == 1024KB == 1 MB
        prop.setLanguageKey(getLanguageKey("max_filesize_kb"));
        maxFileSize = (prop.getInt(DEFAULT_maxFileSize) * 1024);
        prop.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
        propOrder.add(prop.getName());

        prop = config.get(Configuration.CATEGORY_GENERAL, "max_image_size", DEFAULT_maxPaintingSize, "", 64, 128);
        prop.setLanguageKey(getLanguageKey("max_image_size"));
        maxPaintingSize = prop.getInt(DEFAULT_maxPaintingSize);
        prop.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
        propOrder.add(prop.getName());

        prop = config.get(Configuration.CATEGORY_GENERAL, "date_format", DEFAULT_paintingDateFormat);
        prop.setLanguageKey(getLanguageKey("date_format"));
        paintingDateFormat = prop.getString();
        prop.setValidValues(new String[] { "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MMM-yyyy", "yyyy-MMM-dd" });
        propOrder.add(prop.getName());


        config.setCategoryPropertyOrder(Configuration.CATEGORY_GENERAL, propOrder);



        cat = config.getCategory(CATEGORY_PAINTINGS);
        cat.setComment(CustomPaintingConfigItem.getArrayDescription());


        // Loads custom paintings
        LogHelper.info("Loading custom paintings config info...");
        mahPaintings = new ArrayList<CustomPaintingConfigItem>();

        for (final Property item : cat.getOrderedValues()) {
            if (item.getName().startsWith(PAINTINGS_ARRAY_KEY)) {
                final String[] content = item.getStringList();
                final CustomPaintingConfigItem configItem = new CustomPaintingConfigItem(content);

                if (configItem.isValid()) {
                    mahPaintings.add(configItem);
                } else {
                    LogHelper.info("    Error loading a config entry: [" + configItem.getValiadtionErrors() + "]");
                }
            }
        }

        LogHelper.info("Loaded complete, [" + mahPaintings.size() + "] entries found.");



        // saving the configuration to its file
        if (config.hasChanged()) {
            config.save();
        }
    }



    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.modID.equalsIgnoreCase(Reference.ModID)) {
            // Resync config
            loadConfig();
        }
    }



    /**
     * Returns the full language key for elements of this GUI.
     */
    protected static String getLanguageKey(String name)
    {
        return "sidben.ateliercanvas.config.prop." + name;
    }


}
