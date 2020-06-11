package com.oda.applications.epic.hapifhirsmart;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Specimen.Treatment;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Bundle;


public class PatientQueriesEpicFhir
{
    public static void main( String[] args )
    {
        String ServerBase = "http://hapi.fhir.org/baseDstu3";
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient(ServerBase);

        // Read patient using MRN
        String mrn = "1425674";
        Patient patient;
        try{
            patient = client.read().resource(Patient.class).withId(mrn).execute();
            System.out.println("Completed!");
        } catch (ResourceNotFoundException e){
            System.out.println("Resource not found!");
            return;
        }

        // Display the query response
        String result = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(result);
    
        //------------------------------------------------------------------------------------------
        //  Search for Patient resources with the family name Smith and print the results
        String familyName="Smith";
        Bundle queryResponse,nextPage;
        try{
            queryResponse = client.search()
                            .forResource(Patient.class)
                            .where(Patient.FAMILY.matches().value(familyName))
                            .returnBundle(Bundle.class)
                            .execute();
            System.out.println("Patient with Family name Smith: ");
            System.out.println(ctx.newXmlParser().encodeResourceToString(queryResponse));
        } catch (ResourceNotFoundException e){
            System.out.println("Search for thr patient resource not Found!");
            return;
        }

        // Loading the response of the next page
        try{
            nextPage = client.loadPage()
                        .next(queryResponse)
                        .execute();

            System.out.println("Patient response for next page for Smith: ");
            System.out.println(ctx.newXmlParser().encodeResourceToString(nextPage));

        }catch (ResourceNotFoundException e){
            System.out.println("Patient response for next page for Smith not Found!");
            return;
        }

        //------------------------------------------------------------------------------------------
        // Create a patient and populate the data

        Patient newPatientRecord = new Patient();
        
        newPatientRecord.addIdentifier()
            .setSystem("http://hapi.fhir.org/baseDstu3/mrn")
            .setValue("34567");

        newPatientRecord.addName()
            .setFamily("Chalmers")
            .addGiven("Peter")
            .addGiven("James");
            
        newPatientRecord.addTelecom()
            .setSystem(ContactPointSystem.PHONE)
            .setValue("(03) 5555 4567")
            .setUse(org.hl7.fhir.r4.model.ContactPoint.ContactPointUse.WORK);

        newPatientRecord.addTelecom()
            .setSystem(ContactPointSystem.EMAIL)
            .setValue("peter@epic.com");
        
        newPatientRecord.setBirthDateElement(new DateType("2010-06-05"));
        newPatientRecord.setGender(Enumerations.AdministrativeGender.MALE);
            

        //Creating the patient Resource on the server
        MethodOutcome patientCreate;

        try{
            patientCreate = client.create()
                            .resource(newPatientRecord)
                            .execute();
            
            IIdType patientId = patientCreate.getId();
            System.out.println("New Patient Created, ID: " +patientId);
        } catch(ResourceNotFoundException e){
            System.out.println("New patient record could not be created. "+ e);
            return;
        }

    }
}
