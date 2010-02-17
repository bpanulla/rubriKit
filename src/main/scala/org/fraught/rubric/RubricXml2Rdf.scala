package org.fraught.rubric

import scala.io.Source
import java.io.FileOutputStream
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.rdf.model.RDFList
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.util.FileManager

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.OWL;
import org.fraught.rubric.vocabulary.Rubric;

object RubricXml2Rdf
{
  
  val baseUri = "http://fraught.org/";
  val baseFilesystemPath = "./resources/";

  def main(args : Array[String]) : Unit =
  {
   	// create an empty Model
	var model = ModelFactory.createDefaultModel();
	model.setNsPrefix("rubric", Rubric.getURI);
  
    if (args.length > 0)
    {
      
      val RubricXml = scala.xml.XML.loadFile(args(0))

      val RubricId = RubricXml \ "@id"

      val destinationFilename = ""+ RubricId +".rdf"
      val destinationFilesystemPath = baseFilesystemPath + "generated/" + destinationFilename
      val RubricUri = baseUri + "rubric/" + destinationFilename + "#"
    
      model.setNsPrefix("", RubricUri);

      var RubricType : Resource = null
      
      if ((RubricXml \ "holisticRubric") != "")
        RubricType = Rubric.HolisticRubric
      else if (( RubricXml \ "checklistRubric" ) != "")
        RubricType = Rubric.ChecklistRubric
      else if (( RubricXml \ "analyticRubric" ) != "")
        RubricType = Rubric.AnalyticRubric      
      else
        RubricType = Rubric.Rubric
      
      var thisRubric = model.createResource(RubricUri)
        .addProperty(RDF.`type`, RubricType)
      
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
        var elementList : List[Resource] = Nil
        
        for (element <- (category \ "assessmentElement"))
        {
          
          val elementId = element \ "@id"
          val elementUri = baseUri + "Rubric/element/" + elementId + "#"
          val elementCriterion = (element \ "criterion").text
          val elementDescription = (element \ "description").text
          val elementFeedback = (element \ "feedback").text
          //println(elementCriterion)
          //println(elementDescription)
          
          var thisElement = model.createResource(elementUri)
            .addProperty(RDF.`type`, Rubric.Element)
            .addProperty(Rubric.criterion, elementCriterion)
            .addProperty(Rubric.description, elementDescription)
            .addProperty(Rubric.elementFeedback, elementFeedback)
	      
          elementList = thisElement :: elementList;

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
			 .addProperty(Rubric.levelFeedback, feedback)
            
            levelList = thisLevel :: levelList
	      }
       
          thisElement.addProperty(Rubric.hasLevels, buildList(model, Rubric.LevelList, levelList))
        }
	        
        newCat.addProperty(Rubric.hasElements, buildList(model, Rubric.ElementList, elementList)) 
      }
      
      thisRubric.addProperty(Rubric.hasCategories,  buildList(model, Rubric.CategoryList, catList)) 
      
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

    }
    else
      Console.err.println("Please enter filename")
  }
  
  
  
  def buildList( model : Model, listType : Resource, resList : List[Resource]) : Resource =
  {
    // Start off the list with a nil tail
    var outList = RDF.nil
    
    for (res <- resList)
	{
      outList = buildListItem(model, listType, res, outList)
    }
    
    return outList
  }
  
  
  def buildListItem( model : Model, listType : Resource, res : Resource, list : Resource) : Resource =
  {
    model.createResource()
      .addProperty(RDF.`type`, listType)
      .addProperty(RDF.first, res)
      .addProperty(RDF.rest, list)
  }
}
