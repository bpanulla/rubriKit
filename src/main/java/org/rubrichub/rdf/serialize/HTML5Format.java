package org.rubrichub.rdf.serialize;

import java.io.ByteArrayOutputStream;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

public class HTML5Format
{

	public static String format( ModelCom  model )
	{
		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		model.write(streamOut, "RDF/XML-ABBREV");

		
		
		return streamOut.toString();
	}
}
