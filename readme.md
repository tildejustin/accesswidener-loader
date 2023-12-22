## AccessWidener Loader

Remaps and loads any number of accesswideners from the `.minecraft/config/accesswideners` folder.

note: it can only remap from the mappings minecraft is using. with fabric loader 0.15 and mapping io support, we can now use tiny v2 and thus merged mappings at runtime with `official`, `intermediary`, and `named` mappings all at once. an example on how to do this for legacy fabric 1.3.2 with prism launcher patches is provided below, but a warning: if you don't know what any of this means, this mod probably isn't applicable to you.

```json
{
  "formatVersion": 1,
  "+libraries": [
    {
      "name": "net.legacyfabric:yarn:1.3.2+build.533:mergedv2",
      "url": "https://maven.legacyfabric.net/"
    }
  ],
  "+traits": [
    "noapplet"
  ],
  "name": "Intermediary Mappings",
  "uid": "net.fabricmc.intermediary",
  "version": "1.3.2"
}
```

