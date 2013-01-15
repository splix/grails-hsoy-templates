Hsoy Templates Plugin for Grails
================================

Hsoy Templates is a client- and server- side templating system for web and Java, see https://github.com/splix/hsoy-templates

This plugin is adding a new Application Resource type for Grails 2.0+ (w/ Resource plugin installed)

Latest versions: `0.3`

Install
--------

Add dependency into `BuildConfig.groovy`:

```
plugins {
    compile "org.grails.plugins:hsoy-templates:0.3"
}
```

Put your `.hsoy` files into `web-app/hsoy/` (like `web-app/hsoy/myapp.hsoy`) directory, and configure resources as:

```
templates {
    dependsOn 'soyutils'
    resource url: '/hsoy/myapp.hsoy', attrs:[type:'js']
}
```

License
-------

Licensed under the Apache License, Version 2.0