package org.fraught.rubric

import scala.io.Source
import java.io.FileOutputStream
import com.hp.hpl.jena.rdf.model.{Model,ModelFactory,Resource,ResourceFactory,RDFNode}
import com.hp.hpl.jena.util.FileManager

import com.hp.hpl.jena.vocabulary.{RDF,RDFS,OWL}
import org.rubrichub.rdf.vocab.Rubric

object RubricXml2Rdf
{
	val baseUri = "http://fraught.org/"
	val baseFilesystemPath = "./resources/"
	
	def main(args : Array[String]) : Unit =
	{
		if (args.length > 0)
		{
    	val sourceFile = args(0)

    	// create an empty Model
    	var model = ModelFactory.createDefaultModel();
    	model.setNsPrefix("rubric", Rubric.getURI);
    	
    	val RubricXml = scala.xml.XML.loadFile(sourceFile)

    	val RubricId = RubricXml \ "@id"
    	val destinationFilename = ""+ RubricId +".rdf"
    	val destinationFilesystemPath = baseFilesystemPath + "generated/" + destinationFilename
    	val RubricUri = baseUri + "rubric/" + destinationFilename + "#"

    	model.setNsPrefix("", RubricUri);

    	var RubricType : Resource = null
    	
    	var rubricElement = RubricXml \ "holisticRubric";
  
    	if ((rubricElement).size > 0)
    		RubricType = Rubric.HolisticRubric
    	else if (( RubricXml \ "checklistRubric" ).size > 0)
    		RubricType = Rubric.ChecklistRubric
    	else if (( RubricXml \ "analyticRubric" ).size > 0)
    		RubricType = Rubric.AnalyticRubric      
    	else
    		RubricType = Rubric.Rubric

    	var thisRubric = model.createResource(RubricUri).addProperty(RDF.`type`, RubricType)
    	val RubricTitle = RubricXml \ "@title"
      if ( RubricTitle != "" )
            thisRubric.addProperty(Rubric.title, RubricTitle.toString) 
      
      
      
      /********************************************************************
      * Loop over Categories
      ********************************************************************/

      // create a stack to store the category nodes as we create them
      var catList : List[Resource] = Nil
      
      for (category <- ((RubricXml \\ "categories") \\ "category"))
      {
        //println(category \ "@name")
        
        var newCat = model.createResource()
          .addProperty(RDF.`type`, Rubric.Category)
          .addProperty(Rubric.title, (category \ "@name").toString)

        catList = newCat :: catList; 
          
	    /********************************************************************
	    * Loop over Elements
	    ********************************************************************/
        // create a stack to store the element nodes as we create them
        
        // should no longer be necessary due to the 
        //var elementList : List[Resource] = Nil
        
        val criteriaList = (category \ "assessmentElement").map
        {
          (element) => 
        
          {
	          val elementId = element \ "@id"
	          val elementUri = baseUri + "Rubric/element/" + elementId + "#"
	          val elementCriterion = (element \ "criterion").text
	          val elementDescription = (element \ "description").text
	          val elementFeedback = (element \ "feedback").text
	          //println(elementCriterion)
	          //println(elementDescription)
	          
	          var thisElement = model.createResource(elementUri)
	            .addProperty(RDF.`type`, Rubric.Criterion)
	            .addProperty(Rubric.title, elementCriterion)
	            .addProperty(Rubric.description, elementDescription)
	            .addProperty(Rubric.feedback, elementFeedback)
		      
	
	          
	          /********************************************************************
		      * Loop over Levels
		      ********************************************************************/
	          // create a stack to store the level nodes as we create them
		      var levelList : List[Resource] = Nil
		      
		      for (level <- (element \ "level"))
	          {
	            var points = level \ "@points"
	            var benchmark = (level \ "benchmark").text
	            var feedback = (level \ "feedback").text
	            
	            var thisLevel = model.createResource()
			     .addProperty(RDF.`type`, Rubric.Level)
				 .addProperty(Rubric.score, points.toString)
	             .addProperty(Rubric.benchmark, benchmark)
				 .addProperty(Rubric.feedback, feedback)
	            
	            levelList = thisLevel :: levelList
		      }
	       
	          thisElement.addProperty(Rubric.hasLevels, buildList(model, Rubric.LevelList, levelList))
	        
	          thisElement // Acts like return statement
          } 
        }
        newCat.addProperty(Rubric.hasCriteria, buildList(model, Rubric.CriteriaList, criteriaList)) 
        
      }
      
      thisRubric.addProperty(Rubric.hasCriteria,  buildList(model, Rubric.CriteriaList, catList)) 
      
      print("Writing file...")
      var fout = new FileOutputStream(destinationFilesystemPath)
      model.write(fout, "RDF/XML-ABBREV")
      println("Done.")
      print("Validating....")
      
      var inModel = ModelFactory.createDefaultModel();
      
      var in = FileManager.get().open( destinationFilesystemPath );
      if (in == null)
      {
		throw new IllegalArgumentException("File: " + destinationFilesystemPath + " not found");
      }
      
      // read the RDF/XML file
      model.read(in, null);
      println("Done.")

    }
    else
      Console.err.println("Please enter filename")
  }
  
  def rubricXmlToModel( rubricXml : String ) : Model =
  {
	  var model = ModelFactory.createDefaultModel()
	  
	  return model
  }
  
  
  
  
  def buildList( model : Model, listType : Resource, resList : Seq[Resource]) : Resource =
  {
    // Start off the list with a nil tail
    var outList = RDF.nil
    
    for (res <- resList)
	{
     outList = buildListItem(res)
    }
  
    def buildListItem (res : Resource) : Resource =
	{
	    model.createResource()
	      .addProperty(RDF.`type`, listType)
	      .addProperty(RDF.first, res)
	      .addProperty(RDF.rest, outList)
	}
	    
    return outList
  }
}
