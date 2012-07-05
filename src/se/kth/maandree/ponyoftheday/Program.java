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
    
    
    public static final HashMap<String, String[][]> commands = new HashMap<String, String[][]>();
    
    
    public static void main(final String... args) throws Exception
    {
	int _port = PORT;
	int _interval = 12 * 60 * 60;
	String _commands = "/usr/share/pony-of-the-day";
	
	for (int i = 0, n = args.length; i < n; i++)
	    if      (args[i].equals("--port"))      _port     = Integer.parseInt(args[++i]);
	    else if (args[i].equals("--interval"))  _interval = Integer.parseInt(args[++i]);
	    else if (args[i].equals("--commands"))  _commands = args[++i];
	
	final int port = _port;
	final long interval = _interval * 1000L;
	final String commandsFile = _commands;
	
	final HashMap<InetAddress, long[]> timemap = new HashMap<InetAddress, long[]>();
	
	try (final InputStream is = new BufferedInputStream(new FileInputStream(new File(commandsFile))))
	{   try (final Scanner sc = new Scanner(is))
	    {   while (sc.hasNextLine())
		{   final String line = sc.nextLine();
		    if (line.contains(":") == false)
			continue;
		    String[] strs = (line.startsWith(":") ? ("default" + line) : line).split(":");
		    final String key = strs[0];
		    strs = strs[1].replace(" | ", "|").split("\\|");
		    final String[][] value = new String[strs.length][];
		    for (int i = 0, n = strs.length; i < n; i++)
			value[i] = strs[i].split(" ");
		    commands.put(key, value);
        }   }   }
	
        (new Thread()
            {   @Override
                public void run()
        	{   try (final DatagramSocket socket = new DatagramSocket(port))
                    {   for (;;)
                        {   final byte[] buf = new byte[32];
                            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            socket.receive(packet);
                            (new Thread()
               	                {   @Override
			            public void run()
				    {   try (final DatagramSocket socket = new DatagramSocket(port))
					{   for (;;)
					    {   final long now = System.currentTimeMillis();
						final long[] last = timemap.get(packet.getAddress());
						if ((last != null) && (now - last[0] < interval))
						    continue;
						timemap.put(packet.getAddress(), new long[] { now });
						final String mode = new String(buf, 0, packet.getLength(), "UTF-8");
						final byte[] ret = exec(mode);
						socket.send(new DatagramPacket(ret, ret.length, packet.getSocketAddress()));
				        }   }
					catch (final Throwable err)
					{   err.printStackTrace(System.err);
				}   }   }
				).start();
		    }   }
		    catch (final Throwable err)
		    {   err.printStackTrace(System.err);
	    }   }   }
	    ).start();
	
	try (final ServerSocket servsock = new ServerSocket(port))
	{   for (;;)
	    {   final Socket socket = servsock.accept();
	        (new Thread()
		    {   @Override
		        public void run()
		        {   try (final OutputStream os = socket.getOutputStream())
			    {   final long now = System.currentTimeMillis();
				final long[] last = timemap.get(socket.getInetAddress());
				if ((last != null) && (now - last[0] < interval))
				    return;
				timemap.put(socket.getInetAddress(), new long[] { now });
				os.write(exec("default"));
				os.flush();
			    }
			    catch (final Throwable err)
			    {   err.printStackTrace(System.err);
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

