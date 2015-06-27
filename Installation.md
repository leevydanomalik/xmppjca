## What you will need ##

Please make sure that you have the following software already installed on your machine.

  * [Java 5](http://java.sun.com) (or greater)
  * [ANT 1.7](http://ant.apache.org) (or greater)
  * [GlassFish V2](https://glassfish.dev.java.net)
  * [Eclipse IDE for Java EE Developers (version 3.3)](http://www.eclipse.org) (optional)
  * [Polarion Subversive SVN plugin](http://www.polarion.org/index.php?page=overview&project=subversive) for Eclipse (optional)


---


## How to get started with XMPPJCA in 6 Steps ##

To work with XMPPJCA, you will need to build and deploy both the resource adapter (.rar file) and an enterprise application (.ear file) onto your GlassFish server. But first you will need to check-out the XMPPJCA project from SVN (hosted by Google Code).

  * [Step 1 Checkout the source code](Installation#Step_1_Checkout_the_source_code.md)
  * [Step 2 Create your project properties file](Installation#Step_2_Create_your_project_properties_file.md)
  * [Step 3 Build The Resource Adapter and the Enterprise Application](Installation#Step_3_Build_The_Resource_Adapter_and_the_Enterprise_Application.md)
  * [Step 4 Deploy the Resource Adapter](Installation#Step_4_Deploy_the_Resource_Adapter.md)
  * [Step 5 Deploy the Enterprise Application](Installation#Step_5_Deploy_the_Enterprise_Application.md)
  * [Step 6 Test the Installation](Installation#Step_6_Test_the_Installation.md)


---


### Step 1 Checkout the source code ###

To check-out the source code, refer to the [SVN repository check-out instructions](http://code.google.com/p/xmppjca/source/checkout), or have a look at the screenshots below showing how you can use Eclipse to add the repository to your workspace and then check-out the project. You will need to install the  [Polarion Subversive SVN plugin](http://www.polarion.org/index.php?page=overview&project=subversive) first though.

**Figure 1: Add the SVN repository location to your workspace**

![http://xmppjca.googlecode.com/files/xmppjca-svn-repository.png](http://xmppjca.googlecode.com/files/xmppjca-svn-repository.png)

**Figure 2: Check out the 'grapevineim-xmpp' project from 'trunk'**

![http://xmppjca.googlecode.com/files/xmppjca-svn-check-out.png](http://xmppjca.googlecode.com/files/xmppjca-svn-check-out.png)

Your file structure will look something like Figure 3 below.

**Figure 3: Project structure**

![http://xmppjca.googlecode.com/files/xmppjca-project-structure.png](http://xmppjca.googlecode.com/files/xmppjca-project-structure.png)

If it's not compiling, check to make sure that your GlassFish server runtime library reference (part of your project Build Path) is bound to the actual location of your GlassFish instance on your machine.

**Figure 4: If compilation fails, check to make sure that your GlassFish runtime is bound to proper location on your machine.**

![http://xmppjca.googlecode.com/files/xmppjca-project-glassfish-unbound.png](http://xmppjca.googlecode.com/files/xmppjca-project-glassfish-unbound.png)

[Back to Top of Page](Installation.md)

---


### Step 2 Create your project properties file ###

Now that you have checked-out the code, go to the project's root directory and create a file called project.properties. The contents of your file will contain project specific information to build everything as well as login credentials for the XMPP messaging server. Here is the set of properties that you will need to define:
```
  # fully qualified path to your project files
  project.home=C:\\eclipse\\workspace\\grapevineim-xmpp

  # fully qualified path to your glassfish installation
  project.j2ee.home=C:\\glassfish

  # project version ID
  project.version=1.0

  # XMPP server hostname
  project.xmpp.host=talk.google.com

  # XMPP server port
  project.xmpp.port=5222

  # XMPP server domain name for user account
  project.xmpp.domain=gmail.com

  # XMPP login username
  project.xmpp.username=ENTER_YOUR_USERNAME_HERE

  # XMPP login password
  project.xmpp.password=ENTER_YOUR_PASSWORD_HERE
```

**Figure 5: project.properties file contents**

![http://xmppjca.googlecode.com/files/xmppjca-project-properties.png](http://xmppjca.googlecode.com/files/xmppjca-project-properties.png)

### Step 2.1 - TODO - Fix this in dependencies.xml ###

In the dependencies.xml file there is a hardcoded path to your project.properties file. You need to manually fix this path on your machine. I will fix this ugly hack very soon!

**Figure 6: Ugly hack for project.properties path in dependencies.xml**

![http://xmppjca.googlecode.com/files/xmppjca-project-dependencies-hack.png](http://xmppjca.googlecode.com/files/xmppjca-project-dependencies-hack.png)

[Back to Top of Page](Installation.md)

---


### Step 3 Build The Resource Adapter and the Enterprise Application ###

Now that you have a project.properties file created, you should be able to build the resource adapter and enterprise application, and package everything up with ant. Run the following commands from the project root directory (or from within Eclipse)
```
  # build the Resource Adapter (i.e. JCA connector)
  ant connector 

  # build the Enterprise Application
  ant app
```

**Figure 7: Running ant build commands from within Eclipse**

![http://xmppjca.googlecode.com/files/xmppjca-project-ant-build.png](http://xmppjca.googlecode.com/files/xmppjca-project-ant-build.png)

You will find the packaged .rar and .ear files in the respective 'target' directories as shown in Figure 8.

**Figure 8: Output files available in 'target' directories**

![http://xmppjca.googlecode.com/files/xmppjca-project-ant-build-output.png](http://xmppjca.googlecode.com/files/xmppjca-project-ant-build-output.png)

[Back to Top of Page](Installation.md)

---


### Step 4 Deploy the Resource Adapter ###

  1. To deploy the resource adapter start your GlassFish server, and then use the asadmin utility to deploy the resource adapter as shown below. The resource adapter will be deployed using the name 'grapevineim-xmpp-connector-1.0'. You will may need to fully qualify the path to the resource adapter file. such as './connector/target/grapevineim-xmpp-connector-1.0.rar'
```
  asadmin> deploy grapevineim-xmpp-connector-1.0.rar
```
  1. Now you'll want to create a connection pool of container-managed connections to the resource adapter. Outbound connections that will be created at runtime will be created by a factory class that implements the com.grapevineim.xmpp.XmppConnectionFactory interface. This interface is defined in the 'api' project and the binding is defined in the ra.xml file (see 'outbound-resourceadapter' element) in file './connector/src/main/resources/META-INF/ra.xml'
```
  asadmin> create-connector-connection-pool --raname grapevineim-xmpp-connector-1.0 --connectiondefinition 
com.grapevineim.xmpp.XmppConnectionFactory XmppMessagingConnectorPool
```
  1. Finally, you'll want to associate the connection pool with a JNDI name. This name is used when doing an explicit JNDI lookup for outbound messaging, and it is also using for inbound messaging as defined in './inbound/source/main/resources/META-INF/sun-ejb-jar.xml' as shown in Figure 9. Basically, when the inbound MDB gets deployed, a JNDI lookup happens  internally in order to register the inbound MDB as the "endpoint" or listener for messages incoming through from the RA (see Step 5 for more info on this).
```
  asadmin> create-connector-resource --poolname XmppMessagingConnectorPool eis/ra/XmppMessagingConnector
```

**Figure 9: sun-ejb-jar.xml res-ref-name refers to the JNDI name of the connection pool**

![http://xmppjca.googlecode.com/files/xmppjca-project-sun-ejb-jar-res-ref-name.png](http://xmppjca.googlecode.com/files/xmppjca-project-sun-ejb-jar-res-ref-name.png)

[Back to Top of Page](Installation.md)

---


### Step 5 Deploy the Enterprise Application ###

The Enterprise Application contains an inbound message-driven bean component as well as an outbound message-driven bean component. When the inbound MDB gets deployed, the container will activate it as an 'endpoint' for inbound messages received from the resource adapter.    In our system design, the inbound MDB will put messages that it receives onto a JMS queue  for further processing. This queue has a JNDI name 'jms/xmpp/messagequeue'.

The outbound MDB simply listens for messages from the same JMS queue. When it gets a new message, it will do a JNDI lookup to get a outbound connection handle for the resource adapter. It then does a small transformation on the inbound message (change 'from' address to 'to' address and change body of message to 'hello'), and then send it back out to the original sender.

  1. Create a JMS resource to create connections to the JMS queue
```
  asadmin> create-jms-resource --restype javax.jms.QueueConnectionFactory jms/XmppQueueConnectionFactory
```
  1. Create a JMS queue. Inbound MDB will enqueue messages. Outbound MDB will dequeue messages.
```
  asadmin> create-jms-resource --restype javax.jms.Queue jms/xmpp/messagequeue
```
  1. Deploy the Enterprise Application (located in './app/target/')
```
  asadmin> deploy grapevineim-xmpp-app-1.0.ear
```
Once your application has been deployed, you should see a message that resembles the following message in your GlassFish server logs
```
LDR5010: All ejb(s) of [grapevineim-xmpp-app-1.0] loaded successfully!|#]
```

[Back to Top of Page](Installation.md)

---


### Step 6 Test the Installation ###

In order to test your installation, you'll need a IM client like the Google Talk IM client or [pidgin](http://pidgin.im) and an IM account with a service provider that supports Jabber / XMPP. I'll be using [Google Talk](http://www.google.com/talk) as my service provider. Google Talk actually integrates with [Google Apps](http://www.google.com/apps/business/index.html) which I am using for my [grapevine.im](http://www.grapevine.im) domain, however I had to [set DNS SRV records for my domain to make Google Talk work with Google Apps](http://rutger.heijmerikx.nl/2007/6/26/google-talk-for-google-apps-srv-records).

In this test case, I'll connect my app to Google Talk as 'alex at grapevine.im'. I'll be able to chat with my app using any other Google Talk account (and hopefully other services who are [federated](http://code.google.com/apis/talk/open_communications.html) with Google Talk). Here is what my project.properties file looks like:
```
  project.home=C:\\eclipse\\workspace\\grapevineim-xmpp
  project.j2ee.home=C:\\glassfish
  project.version=1.0
  project.xmpp.host=talk.google.com
  project.xmpp.port=5222
  project.xmpp.domain=grapevine.im
  project.xmpp.username=alex
  project.xmpp.password=XXXXXXXX
```

I'll add a new buddy as shown in Figures 10 and 11 below.

**Figure 10: Add a new buddy**

![http://xmppjca.googlecode.com/files/xmppjca-im-add-buddy.png](http://xmppjca.googlecode.com/files/xmppjca-im-add-buddy.png)

**Figure 11: Receive welcome message**

![http://xmppjca.googlecode.com/files/xmppjca-im-welcome-msg.png](http://xmppjca.googlecode.com/files/xmppjca-im-welcome-msg.png)

Then, I will send a message to my app and receive the messages shown in Figure 12.

**Figure 12: Send a message and receive a response (string reversed)**

![http://xmppjca.googlecode.com/files/xmppjca-im-reverse-strings.png](http://xmppjca.googlecode.com/files/xmppjca-im-reverse-strings.png)

You should notice new log messages in your GlassFish logs as shown below.
```
  <MDB> ---- Inbound MDB got a message: hi there how are you today?|#]
  <MDB> In XmppOutboundMDB.XmppMessageBean()|#]
  <MDB> In XmppOutboundMDB.setMessageDrivenContext()|#]
  <MDB> In XmppOutboundMDB.ejbCreate()|#]
  <MDB> ---- Outbound MDB got a message:  com.sun.messaging.jms.ra.DirectObjectPacket@5e33d4|#]
  sendMessage(XmppMessage)|#]
```

That's it! Hopefully, the installation worked well for you, but if you have any trouble, please feel free to contact me through by [support](Support.md) page.

[Back to Top of Page](Installation.md)

---


### References ###

For further information, your can refer to these links:

  * [Getting Started with Connectors in Sun Java System Application Server Platform Edition 8.0](http://developers.sun.com/appserver/reference/techart/as8_connectors/)
  * [Google Talk Federates with Jabber Servers](http://googletalk.blogspot.com/2006/01/xmpp-federation.html)