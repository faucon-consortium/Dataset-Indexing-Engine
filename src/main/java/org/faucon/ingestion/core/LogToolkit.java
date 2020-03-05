package org.faucon.ingestion.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service("LogToolkit")
public class LogToolkit {

	public List<String> listFiles(String repository, List<String> filestoList ) {
		
		//"Test?/sample*.txt"
		Stream<Path> walk = null;
		List<String> result = new ArrayList<String>();
		List<String>  finalResult= new ArrayList<String>();
		Path pathDir = Paths.get(repository);
		
		for ( String file:filestoList) {
			
			DirectoryStream<Path> dirStream = null ;
			try {
				dirStream = Files.newDirectoryStream(pathDir, file);
				
//				Iterator<Path> it = dirStream.iterator();
//				
//				while (it.hasNext()) {
//					Path p = it.next();
//
//					String tt = p.toString();
//					
//					String root = p.getRoot().toString();
//					String parent = p.getParent().toString();
//					String filesys = p.getFileSystem().toString();
//					
//				}

				
				
				//dirStream.forEach(path -> result.add(path.getParent().toString()+"/"+path.getFileName().toString()));
				dirStream.forEach(path -> result.add(path.toString()));
				
				finalResult.addAll(result);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		
//		try {
//			walk = Files.walk(Paths.get("/home/chdor/Projects/FAUCON/cloud-maintenance/dataset/2020-02-15_21"));
//			result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
//	
//			DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(".."), "Test?/sample*.txt");
//			
//			
//			 try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
//			            Paths.get(".."), "Test?/sample*.txt")) {
//			        dirStream.forEach(path -> System.out.println(path));
//			    }
//			
//			
//			
//			result.forEach(System.out::println);
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return finalResult;
	}

	
	
	
	public static void getFilesDate(List<String> result) {
		
		File file=null;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String datefile = null;
		
		for (String fileName:result ) {
			file = new File(fileName);
			file.lastModified();
			
			datefile = sdf.format(file.lastModified());
			System.out.println(fileName+"\nDate: "+datefile);
		}
		
	}
	
	public static void getFirstDateandLastDateFromFileContent() {
		
	}
	
	
	public static void getFirstLine(List<String> files) {
		//{"timestamp":"2019-03-08T16:34:00.271134+0100"
		
		BufferedReader reader =null;
		String line = null;
		String tmp = null;
		
		for( String fileName:files) {
		
			try {
				reader = new BufferedReader(new FileReader(fileName));
				try {
					line = reader.readLine();
					

					if (line.startsWith("{\"timestamp\"")) {
						tmp = line.substring(0,47);
						line = line.substring(2, 46);
						line = line.substring(0, line.lastIndexOf("."));
						System.out.println(tmp+" / "+line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	public static String tail( File file ) {
	    RandomAccessFile fileHandler = null;
	    try {
	        fileHandler = new RandomAccessFile( file, "r" );
	        long fileLength = fileHandler.length() - 1;
	        StringBuilder sb = new StringBuilder();

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	            if( readByte == 0xA ) {
	                if( filePointer == fileLength ) {
	                    continue;
	                }
	                break;

	            } else if( readByte == 0xD ) {
	                if( filePointer == fileLength - 1 ) {
	                    continue;
	                }
	                break;
	            }

	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        if (fileHandler != null )
	            try {
	                fileHandler.close();
	            } catch (IOException e) {
	                /* ignore */
	            }
	    }
	}

	public String tail2( File file, int lines) {
	    java.io.RandomAccessFile fileHandler = null;
	    try {
	        fileHandler = 
	            new java.io.RandomAccessFile( file, "r" );
	        long fileLength = fileHandler.length() - 1;
	        StringBuilder sb = new StringBuilder();
	        int line = 0;

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	             if( readByte == 0xA ) {
	                if (filePointer < fileLength) {
	                    line = line + 1;
	                }
	            } else if( readByte == 0xD ) {
	                if (filePointer < fileLength-1) {
	                    line = line + 1;
	                }
	            }
	            if (line >= lines) {
	                break;
	            }
	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    }
	    finally {
	        if (fileHandler != null )
	            try {
	                fileHandler.close();
	            } catch (IOException e) {
	            }
	    }
	}	
	
}
