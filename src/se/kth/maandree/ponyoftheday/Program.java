package se.kth.maandree.ponyoftheday;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Program
{
    public static final int PORT = 17;
    
    
    public static final HashMap<String, String[][]> commands = new HashMap<>();
    
    
    public static void main(final String... _args) throws Exception
    {
	try (final InputStream is = new BufferedInputStream(new FileInputStream(new File("/usr/share/pony-of-the-day"))))
	{   try (final Scanner sc = new Scanner(is))
	    {   while (sc.hasNextLine())
		{   final String line = sc.nextLine();
		    if (line.contains(":") == false)
			continue;
		    String[] strs = (line.startsWith(":") ? ("default" + line) : line).split(":");
		    final String key = strs[0];
		    strs = strs[1].replace(" | ", "|").split("|");
		    final String[][] value = new String[strs.length][];
		    for (int i = 0, n = strs.length; i < n; i++)
			value[i] = strs[i].split(" ");
		    commands.put(key, value);
        }   }   }
	
        (new Thread()
            {   @Override
                public void run()
        	{   try (final DatagramSocket socket = new DatagramSocket(PORT))
                    {   for (;;)
                        {   final byte[] buf = new byte[32];
                            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            socket.receive(packet);
                            (new Thread()
               	                {   @Override
			            public void run()
				    {   try (final DatagramSocket socket = new DatagramSocket(PORT))
					{   for (;;)
					    {   final String mode = new String(buf, 0, packet.getLength(), "UTF-8");
						final byte[] ret = exec(mode);
						socket.send(new DatagramPacket(ret, ret.length, packet.getSocketAddress()));
				        }   }
					catch (final Throwable err)
					{   //Do nothing
				}   }   }
				).start();
		    }   }
		    catch (final Throwable err)
		    {   //Do nothing
	    }   }   }
	    ).start();
	
	try (final ServerSocket servsock = new ServerSocket(PORT))
	{   for (;;)
	    {   final Socket socket = servsock.accept();
	        (new Thread()
		    {   @Override
		        public void run()
		        {   try (final OutputStream os = socket.getOutputStream())
			    {   os.write(exec("default"));
				os.flush();
			    }
			    catch (final Throwable err)
			    {   //Do nothing
			    }
			    finally
			    {   try
			        {   socket.close();
			        }
			        catch (final Throwable err)
			        {   //Do nothing
		    }   }   }   }
	    )       .start();}
	}
    }
    
    
    public static byte[] exec(final String mode) throws Exception
    {
	String[][] args = commands.get(mode);
	if (args == null)
	    args = commands.get("default");
	
	byte[] buf = null;
	int len = 0;
	
	for (int i = 0, n = args.length; i < n; i++)
	{   final Process proc;
	    (proc = (new ProcessBuilder(args[i])).start()).waitFor();
	    if (buf != null)
		try (final OutputStream os = proc.getOutputStream())
		{   os.write(buf, 0, len);
		    os.flush();
		}
	    try (final InputStream is = proc.getInputStream())
	    {   if ((buf == null) || (is.available() > len))
		    buf = new byte[is.available()];
		len = is.available();
		is.read(buf);
	}   }
	
	byte[] rc = buf;
	if (buf.length != len)
	{   rc = new byte[len];
	    System.arraycopy(buf, 0, rc, 0, len);
	}
	return rc;
    }
    
}

