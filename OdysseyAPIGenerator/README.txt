README
------
  Odyssey API Generator is an open source project for creating a shared library based on Tyler 
  Technologies' XSD files. The purpose of the library is to abstract the programmer from the details of 
  XML parsing, XML to Java (POJO) marshalling or unmarshalling, web services calls to Odyssey, and allow the 
  the programmer to work with plain old java objects (POJOs) directly.
  
  Project files are available in the public repository available at https://github.com/rperalta1/OdysseyAPIGenerator.

  Please refer to the OdyApiGen-ParentProject/docs directory in order to find the following documents that 
  explain both how to build the shared library as well as how it works:

    - BUILD_Cli.txt explains how to build the library from the command line interface.
    - BUILD_Eclipse.docx shows how to load and build the project using the Eclipse IDE.
    - Odyssey API Library Overview explains the logic and concepts used in this project.

  The building of this library has been tested against these Odyssey (major) releases/versions:
     * 2013 - no known issues  
     * 2014 - no known issues
     * 2017 - several known issues due to errors in some XSD files within this release of Odyssey; 
              the issues with Odyssey 2017 XSD files are discussed in the Odyssey API Library Overview document.
     * 2018 - has not yet been tested

----
END  
