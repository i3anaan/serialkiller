serialkiller
============

![logo](http://i.imgur.com/RV0ugtw.png)

SerialKiller is our implementation for the network protocol stack for the
Telematics Project. It is an attempt to destroy serial cables^W^W^Wset serial
ports ablaze^W^W^Wprovide reliable, fast transport over a serial cable.

It's so robust, it kills.

Routes file
-----------

The routes file should be located in the SerialKiller application folder. This
folder is different for various operating systems. For example:

- Windows: ```C:\Users\<Current user>\serialkiller\routes.txt```
- Linux: ```~/serialkiller/routes.txt```

The syntax for the routes file is very easy. Each instruction should be placed
on a new line.

**```{keyword}={tpp_addr}```**

Assigns a TPP address to a specific host identified by the keyword.

```{keyword}```: ```self``` (determines the address of this host) or
```sibling``` (determines the address of the sibling host, connected by the
serial cable)

```{tpp_addr}```: The valid TPP address for the host.

**```{tpp_addr1}>{tpp_addr2}```**

A route in the network. Routes can be chained, for example ```1>2``` and
```2>3``` will route packets for host ```1``` through host ```3```.

```{tpp_addr1}```: A valid TPP address for the destination host.

```{tpp_addr2}```: A valid TPP address for the host the packets need to be
routed through.

**```{tpp_addr}={ip_addr|hostname}```**

Assigns an IP address (or hostname) to a host. This will result in a tunnel
between this host and the host determined by ```{tpp_addr}```.

```{tpp_addr}```: The valid TPP address for the remote host.

```{ip_addr|hostname}```: A valid IP address or hostname for the remote host.
