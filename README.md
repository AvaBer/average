# Average Duration Plugin

## Installation and requirements
The artifact is built using apache-maven and Java.
~~~
1. Build the artifact in cmd/terminal with the command:
    "mvn compile package" or "mvn clean compile package" if the directory "target" exists.
2. Within jenkins go to the page:
    manage jenkins -> plugin manager -> advanced
3. Press "choose file" in the upload plugin section
    select the built artifact in target/averageduration.hpi then press "upload"
~~~
## TLDR;
The plugin displays an average build time estimate of a job in the job/api page,
and optionally as a view-column on the dashboard or as a field in the side panel of the main page of a job.
How the average is calculated can be configured on the "Configure System" page under "Manage jenkins".

How average is produced is a modified version of the implementation in the Job class in the Jenkins core.
### The plugin allows the average to be configured to:
~~~
- Have a larger build history scope to find successful builds
- Change the number of builds that will be used to calculate the average
- Display the average build duration on the main page of a job
~~~
## How it works
The default implementation searches for 3 Successful candidates within 6 latest builds.
Failing to find these completed but failed builds (never aborted) are used as backup candidates.
The last successful build is always added if present.


The plugins implementation behaves the same way except the number of candidates and the history scope can be changed.
By allowing the build history scope to be changed can increase the chance for successful builds to be found,
being able to set the candidate pool may not be particularly useful in most cases but it is there if needed.

## Additional information about what does what and where.

#### AverageDurationDescriptor
This class is where the configuration is persisted and where the other classes can access a copy of it,
to then pass it to their instance of the JobWrapper class. <br>
The config instance is saved as .xml file in jenkins root.

Config.jelly in the AverageDurationDescriptor sub-directory in the resources folder is
where the section of the configuration page is written.

The form-validation for the input is also done here.

(Jenkins manages this instance and supplies it where it is needed.)

#### AverageDurationConfiguration
Savable instance containing the values used in the job wrapper.

#### JobWrapper
This is where the average is calculated by being passed a Job and a Config instance.

#### AverageDurationViewColumn
Allows the average duration to be displayed as a view column, 
disabled by default but available if desired when editing a view.

#### AbstractAverageDurationAction
This abstract class contains what is needed for the extending classes to be displayed on the API and Job main page,
by implementing the Action interface and using the @ExportedBean with the getApi() method.

Having this extra layer allows the extending classes to be less cluttered, 
it contains the instances needed for the wrapper to calculate the average, and the two most basic methods
used to display the average where needed.

#### AverageDurationAction
This class extends AbstractAverageDurationAction, other than the functionality inherited from its super class
it contains one additional method that is exported that only is shown when a job is building something.
This is also where the information shown on the job page accesses its data.

action.jelly in resources/.../AverageDurationAction directory is the view for this class.

The action.jelly file in itself contains a lot of the data-binding needed to display things on the job page,
by using this class.

#### AverageDurationActionFactory
This is the extension point for the AverageDurationAction, it extends TransientActionFactory and creates a new instance
of the action for the job. 

In hindsight passing an action to a job that contains an instance of the job itself inside it might not be the best way 
to do it, but passing the job instance through the view then passing it around to each method that need it might limit 
what the action can export to the API.
