package me.StevenLawson.TotalFreedomMod.HTTPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.StevenLawson.TotalFreedomMod.HTTPD.NanoHTTPD.HTTPSession;
import me.StevenLawson.TotalFreedomMod.HTTPD.NanoHTTPD.Response;
import me.StevenLawson.TotalFreedomMod.TFM_ConfigEntry;
import me.StevenLawson.TotalFreedomMod.TFM_Log;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;

public class TFM_HTTPD_Manager
{
    @Deprecated
    public static String MIME_DEFAULT_BINARY = "application/octet-stream";
    //
    private static final Pattern EXT_REGEX = Pattern.compile("\\.([^\\.\\s]+)$");
    //
    public static final int PORT = TFM_ConfigEntry.HTTPD_PORT.getInteger();
    //
    private final TFM_HTTPD httpd = new TFM_HTTPD(PORT);

    private TFM_HTTPD_Manager()
    {
    }

    public void start()
    {
        if (!TFM_ConfigEntry.HTTPD_ENABLED.getBoolean())
        {
            return;
        }

        try
        {
            httpd.start();

            if (httpd.isAlive())
            {
                TFM_Log.info("TFM HTTPd started. Listening on port: " + httpd.getListeningPort());
            }
            else
            {
                TFM_Log.info("Error starting TFM HTTPd.");
            }
        }
        catch (IOException ex)
        {
            TFM_Log.severe(ex);
        }
    }

    public void stop()
    {
        if (!TFM_ConfigEntry.HTTPD_ENABLED.getBoolean())
        {
            return;
        }

        httpd.stop();

        TFM_Log.info("TFM HTTPd stopped.");
    }

    private static enum ModuleType
    {
        DUMP(new ModuleExecutable(false, "dump")
        {
            @Override
            public Response getResponse(HTTPSession session)
            {
                return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "The DUMP module is disabled. It is intended for debugging use only.");
            }
        }),
        HELP(new ModuleExecutable(true, "help")
        {
            @Override
            public Response getResponse(HTTPSession session)
            {
                return new Module_help(session).getResponse();
            }
        }),
        LIST(new ModuleExecutable(true, "list")
        {
            @Override
            public Response getResponse(HTTPSession session)
            {
                return new Module_list(session).getResponse();
            }
        }),
        FILE(new ModuleExecutable(false, "file")
        {
            @Override
            public Response getResponse(HTTPSession session)
            {
                return new Module_file(session).getResponse();
            }
        }),
        SCHEMATIC(new ModuleExecutable(false, "schematic")
        {
            @Override
            public Response getResponse(HTTPSession session)
            {
                return new Module_schematic(session).getResponse();
            }
        }),
        PERMBANS(new ModuleExecutable(false, "permbans")
        {
            @Override
            public Response getResponse(HTTPSession session)
            {
                return new Module_permbans(session).getResponse();
            }
        });
        //
        private final ModuleExecutable moduleExecutable;

        private ModuleType(ModuleExecutable moduleExecutable)
        {
            this.moduleExecutable = moduleExecutable;
        }

        private abstract static class ModuleExecutable
        {
            private final boolean runOnBukkitThread;
            private final String name;

            public ModuleExecutable(boolean runOnBukkitThread, String name)
            {
                this.runOnBukkitThread = runOnBukkitThread;
                this.name = name;
            }

            public Response execute(final HTTPSession session)
            {
                try
                {
                    if (this.runOnBukkitThread)
                    {
                        return Bukkit.getScheduler().callSyncMethod(TotalFreedomMod.plugin, new Callable<Response>()
                        {
                            @Override
                            public Response call() throws Exception
                            {
                                return getResponse(session);
                            }
                        }).get();
                    }
                    else
                    {
                        return getResponse(session);
                    }
                }
                catch (Exception ex)
                {
                    TFM_Log.severe(ex);
                }
                return null;
            }

            public abstract Response getResponse(HTTPSession session);

            public String getName()
            {
                return name;
            }
        }

        public ModuleExecutable getModuleExecutable()
        {
            return moduleExecutable;
        }

        private static ModuleType getByName(String needle)
        {
            for (ModuleType type : values())
            {
                if (type.getModuleExecutable().getName().equalsIgnoreCase(needle))
                {
                    return type;
                }
            }
            return FILE;
        }
    }

    private static class TFM_HTTPD extends NanoHTTPD
    {
        public TFM_HTTPD(int port)
        {
            super(port);
        }

        public TFM_HTTPD(String hostname, int port)
        {
            super(hostname, port);
        }

        @Override
        public Response serve(HTTPSession session)
        {
            Response response;

            try
            {
                final String[] args = StringUtils.split(session.getUri(), "/");
                final ModuleType moduleType = args.length >= 1 ? ModuleType.getByName(args[0]) : ModuleType.FILE;
                response = moduleType.getModuleExecutable().execute(session);
            }
            catch (Exception ex)
            {
                response = new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error 500: Internal Server Error\r\n" + ex.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(ex));
            }

            if (response == null)
            {
                response = new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404: Not Found - The requested resource was not found on this server.");
            }

            return response;
        }
    }

    public static Response serveFileBasic(File file)
    {
        Response response = null;

        if (file != null && file.exists())
        {
            try
            {
                String mimetype = null;

                Matcher matcher = EXT_REGEX.matcher(file.getCanonicalPath());
                if (matcher.find())
                {
                    mimetype = Module_file.MIME_TYPES.get(matcher.group(1));
                }

                if (mimetype == null || mimetype.trim().isEmpty())
                {
                    mimetype = MIME_DEFAULT_BINARY;
                }

                response = new Response(Response.Status.OK, mimetype, new FileInputStream(file));
                response.addHeader("Content-Length", "" + file.length());
            }
            catch (IOException ex)
            {
                TFM_Log.severe(ex);
            }
        }

        return response;
    }

    public static TFM_HTTPD_Manager getInstance()
    {
        return TFM_HTTPDManagerHolder.INSTANCE;
    }

    private static class TFM_HTTPDManagerHolder
    {
        private static final TFM_HTTPD_Manager INSTANCE = new TFM_HTTPD_Manager();
    }
}
