# Publishing Guide for JetBrains Marketplace

This guide covers everything you need to publish the Code Awareness plugin to the JetBrains Marketplace so users can find and install it.

## Prerequisites

### 1. JetBrains Marketplace Account

1. **Create/Login to JetBrains Account**
   - Go to [https://account.jetbrains.com/](https://account.jetbrains.com/)
   - Create an account or login if you already have one

2. **Register as a Plugin Developer**
   - Visit [https://plugins.jetbrains.com/](https://plugins.jetbrains.com/)
   - Click "Publish Plugin" or "Submit a Plugin"
   - Complete the vendor registration process
   - You'll need to provide:
     - Company/Organization name (or personal name)
     - Contact email
     - Website URL (optional but recommended)

### 2. Generate Publishing Token

1. **Get Your Publishing Token**
   - Go to [https://plugins.jetbrains.com/author/me/token](https://plugins.jetbrains.com/author/me/token)
   - Click "Generate Token" or use existing token
   - Copy the token (you'll only see it once!)
   - Store it securely as an environment variable: `PUBLISH_TOKEN`

### 3. Plugin Signing Certificate (Required for Public Plugins)

JetBrains requires all plugins to be signed. You have two options:

#### Option A: Use JetBrains Certificate (Recommended for First-Time Publishers)

JetBrains can provide a signing certificate. Contact them through the marketplace support.

#### Option B: Use Your Own Certificate

1. **Generate a Certificate**
   ```bash
   keytool -genkey -keyalg RSA -keysize 2048 -validity 10000 \
     -alias plugin-signing-key \
     -keystore plugin-signing-keystore.jks \
     -storepass <your-password> \
     -dname "CN=Code Awareness, OU=Development, O=Code Awareness Team, L=City, ST=State, C=US"
   ```

2. **Export Certificate Chain**
   ```bash
   keytool -export -alias plugin-signing-key \
     -keystore plugin-signing-keystore.jks \
     -file certificate-chain.crt
   ```

3. **Set Environment Variables**
   ```bash
   export CERTIFICATE_CHAIN="$(cat certificate-chain.crt)"
   export PRIVATE_KEY="$(cat plugin-signing-keystore.jks | base64)"
   export PRIVATE_KEY_PASSWORD="<your-password>"
   ```

## Required Plugin Assets

### 1. Plugin Icon

Create a plugin icon (SVG format, recommended size: 40x40 to 128x128):

1. **Create `src/main/resources/META-INF/pluginIcon.svg`**
   - Should be a square SVG
   - Represents your plugin visually
   - Will be displayed in the marketplace and IDE

### 2. Update plugin.xml

Ensure your `plugin.xml` has all required fields:

- ✅ Plugin ID (already set: `com.codeawareness.pycharm`)
- ✅ Plugin name (already set: `Code Awareness`)
- ✅ Vendor information (already set)
- ✅ Description with HTML formatting (already set)
- ✅ Version information (handled by `patchPluginXml` task)

### 3. Add Version Information

The `build.gradle.kts` already has `patchPluginXml` configured. You may want to add:

```kotlin
patchPluginXml {
    sinceBuild.set("233")
    untilBuild.set(provider { null })
    
    // Add these for better marketplace presentation:
    version.set(project.version.toString())
    changeNotes.set("""
        <![CDATA[
        <h3>Initial Release</h3>
        <ul>
            <li>Real-time peer code highlighting</li>
            <li>Conflict detection</li>
            <li>Overlap detection</li>
        </ul>
        ]]>
    """.trimIndent())
}
```

### 4. Create CHANGELOG.md

Create a changelog file for version history:

```markdown
# Changelog

## [1.0.0] - YYYY-MM-DD

### Added
- Initial release
- Real-time peer code highlighting
- Conflict detection
- Overlap detection
- Side-by-side diff viewing
- Branch comparison
```

## Build Configuration

Your `build.gradle.kts` already has the necessary tasks configured:

- ✅ `signPlugin` - Signs the plugin with certificate
- ✅ `publishPlugin` - Publishes to JetBrains Marketplace

## Publishing Steps

### Step 1: Build and Test Locally

```bash
# Build the plugin
./gradlew buildPlugin

# Test in a sandbox IDE
./gradlew runIde

# Verify the plugin works correctly
```

### Step 2: Set Environment Variables

Before publishing, set these environment variables:

```bash
export PUBLISH_TOKEN="your-publishing-token-here"
export CERTIFICATE_CHAIN="your-certificate-chain"
export PRIVATE_KEY="your-private-key-base64"
export PRIVATE_KEY_PASSWORD="your-keystore-password"
```

**For CI/CD (GitHub Actions, etc.):**
- Store these as secrets in your repository settings
- Never commit these values to version control

### Step 3: Build and Sign Plugin

```bash
# Build and sign the plugin
./gradlew buildPlugin signPlugin

# This creates: build/distributions/kawa-pycharm-1.0.0.zip
```

### Step 4: Publish to Marketplace

```bash
# Publish to JetBrains Marketplace
./gradlew publishPlugin
```

This will:
1. Build the plugin
2. Sign it with your certificate
3. Upload it to JetBrains Marketplace
4. Submit it for review

### Step 5: Marketplace Review

After publishing:

1. **Wait for Review**
   - JetBrains reviews all new plugins (usually 1-3 business days)
   - They check for:
     - Code quality
     - Security issues
     - Compliance with marketplace guidelines
     - Proper metadata

2. **Respond to Feedback**
   - Check your email for review feedback
   - Address any issues they find
   - Resubmit if needed

3. **Plugin Goes Live**
   - Once approved, your plugin appears in the marketplace
   - Users can install it via: `Settings > Plugins > Marketplace`

## Updating the Plugin

For future releases:

1. **Update Version**
   ```kotlin
   // In build.gradle.kts
   version = "1.0.1"
   ```

2. **Update CHANGELOG.md**
   - Add new version entry
   - Document changes

3. **Update Change Notes in build.gradle.kts**
   ```kotlin
   patchPluginXml {
       changeNotes.set("""
           <![CDATA[
           <h3>1.0.1</h3>
           <ul>
               <li>Bug fixes</li>
               <li>Performance improvements</li>
           </ul>
           ]]>
       """.trimIndent())
   }
   ```

4. **Publish Update**
   ```bash
   ./gradlew publishPlugin
   ```

## Marketplace Listing Requirements

When your plugin is submitted, ensure the marketplace listing includes:

1. **Clear Description**
   - What the plugin does
   - Key features
   - Use cases

2. **Screenshots** (Optional but Recommended)
   - Add screenshots to your plugin repository
   - They can be referenced in the marketplace listing

3. **Tags/Categories**
   - Select appropriate categories
   - Add relevant tags for discoverability

4. **Compatibility**
   - Specify which IDEs it supports (PyCharm, IntelliJ IDEA, etc.)
   - Your `intellij.type` setting determines this

## Supporting Multiple IDEs

Your current configuration targets IntelliJ IDEA Community (`type.set("IC")`). To support PyCharm specifically:

```kotlin
intellij {
    version.set("2023.3")
    type.set("PC") // PyCharm Professional
    // OR
    type.set("PY") // PyCharm Community
    // OR use multiple types
}
```

For broader compatibility, you can publish separate versions or use:
```kotlin
type.set("IC") // Works with both IDEA and PyCharm
```

## Troubleshooting

### Common Issues

1. **"Invalid token" error**
   - Regenerate your publishing token
   - Ensure `PUBLISH_TOKEN` environment variable is set correctly

2. **"Certificate not found" error**
   - Verify certificate environment variables are set
   - Check certificate format (should be base64 for private key)

3. **"Plugin validation failed"**
   - Check plugin.xml for required fields
   - Ensure plugin icon exists
   - Verify version format is correct

4. **"Build compatibility" warnings**
   - Update `sinceBuild` to match your target IDE version
   - Test with different IDE versions

## Additional Resources

- [JetBrains Plugin Publishing Guide](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html)
- [Plugin Signing Documentation](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
- [Marketplace Guidelines](https://plugins.jetbrains.com/docs/marketplace/marketplace-guidelines.html)
- [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)

## Quick Checklist

Before publishing, ensure:

- [ ] JetBrains Marketplace account created
- [ ] Publishing token generated and stored securely
- [ ] Plugin signing certificate obtained/created
- [ ] Plugin icon created (`pluginIcon.svg`)
- [ ] `plugin.xml` has all required metadata
- [ ] `CHANGELOG.md` created
- [ ] Plugin tested locally
- [ ] Version number set correctly
- [ ] Change notes added to `patchPluginXml`
- [ ] Environment variables configured
- [ ] Plugin builds successfully
- [ ] Plugin signs successfully
- [ ] Ready to submit for review

## Next Steps After Publishing

1. **Monitor Reviews**
   - Check user reviews and ratings
   - Respond to user feedback

2. **Track Usage**
   - Monitor download statistics
   - Track active installations

3. **Maintain Plugin**
   - Fix bugs reported by users
   - Add requested features
   - Keep up with IDE updates

4. **Marketing**
   - Announce on social media
   - Add to project README
   - Share in relevant communities

