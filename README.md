[![Build Status](https://travis-ci.org/dags-/Installer.svg?branch=master)](https://travis-ci.org/dags-/Installer)

For Users
====
1. Install a Forge Modloader profile using the official Forge Installer
2. Run the Modpack Installer, select the above Forge profile and hit 'ok'
3. Launch Minecraft selecting the newly created Modpack profile

For Modpack Creators
====
There are two components to a modpack:  

1. A github repository hosting the modpack files
2. The installer, which is set up to download and install from the repository

## The Repository
The github repository should be structured similarly to the `.minecraft/` directory,
 so would typically look like:  
```
/config/mod_a.cfg
/config/mod_b.cfg
/mods/$mcversion/mod_a.jar
/mods/$mcversion/mod_b.jar
/resourcepacks/some_resource.zip
/options.txt
...
```
Once all files have been uploaded, create a github 'release'. It's recommended
that the 'tag' is set to a logical version number. As you update the repository,
increment the version number accordingly.

## The Installer
Once the repository and release is set up, you need to modify the installer's
`properties.json` file to tell it where your repository is, the name of your
modpack, etc.

The `properties.json` file looks like this:  
_(You should only edit the 'values' on the right-hand side of the ':' character. 
Do not delete the existing quotation marks '"' or commas ',')_
```
{
  "profile_name": "modpack name",
  "target_dir": "profiles",
  "minecraft_version": "1.8.8",
  "github": {
    "repository": "userName/RepositoryName",
    "api" : "https://api.github.com"
  }
}
```
- **profile_name** - The profile name that will be displayed in the Minecraft
 launcher
- **target_dir** - The default location to install to within the `.minecraft/`
directory.
- **minecraft_version** - The version of Minecraft that this modpack is designed for
- **github**
 - **repository** - The location of the modpack repository
 - **api** - The URL of github's api. You shouldn't need to change this
 
#### Notes
**target_dir**  
If left blank, the modpack will be installed directly to the `.minecraft/`
directory. Otherwise it will be installed to `.minecraft/target_dir/profile_name-$tagVersion`  
The user can optionally change this directory as well during installation.

**repository**  
The repository address should be formed from your github username (or organisation
name), followed by a forward slash ('/'), followed by your modpack's repository name. 

## Checklist
- [ ] Create github repository
- [ ] Upload mods/configs/resourcepacks to repository
- [ ] Create github 'Release' for repository
- [ ] Edit the installer `properties.json` file
 - [ ] Set the modpack name (`profile_name`)
 - [ ] Set the Minecraft version (`minecraft_version`)
 - [ ] Set the respository (`repository`)
   - N.B. format should be `{github username}/{repository name}`
- [ ] Test installer, make sure modpack profile correctly runs
- [ ] Distribute installer
