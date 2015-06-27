### XMPPJCA ###

XMPPJCA is a [JCA-compliant Resource Adapter](http://java.sun.com/j2ee/connector/) that lets you connect your J2EE application to an XMPP / Jabber instant messaging service like [Google Talk](http://www.google.com/talk/). It complies with version 1.5 of the Java Connector Architecture specification and supports both inbound and outbound messaging. It is licensed as free, open-source software under the Apache 2.0 license agreement. It has been tested to run on [GlassFish V2](https://glassfish.dev.java.net/), and it is being used currently by [GrapevineIM](http://www.grapevine.im). It is built on top of the [Smack XMPP API](http://www.igniterealtime.org/projects/smack/index.jsp) from Jive Software.

### Binaries ###

You can download the binaries below, but I highly recommend that you read the [installation instructions](Installation.md) first. You will need to change the USERNAME/PASSWORD fields in the 'sun-ejb-jar.xml' file for the inbound message-driven bean used as part of the test app (.ear file). You'll also need to configure other things on your GlassFish server (like message queues and JNDI names).

  * [Download version 1.0 (gzipped tar file)](http://xmppjca.googlecode.com/files/grapevineim-xmpp-1.0.tar.gz)
  * [Download version 1.0 (zipped archive)](http://xmppjca.googlecode.com/files/grapevineim-xmpp-1.0.zip)

### Source Code ###

Check out a read-only working copy anonymously over HTTP.
```
  svn checkout http://xmppjca.googlecode.com/svn/trunk/ xmppjca-read-only
```

### Documentation ###

  * [Installation instructions](Installation.md)

### Support, Feature Requests, and Issues ###

  * [Support information](Support.md)
  * [Submit bugs and feature requests](http://code.google.com/p/xmppjca/issues/list)


---

Copyright 2008 Alex De Marco