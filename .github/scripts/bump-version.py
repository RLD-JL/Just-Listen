import re
import sys

def bump_version(bump_type):
    file_path = "androidApp/build.gradle.kts"
    with open(file_path, "r") as f:
        content = f.read()

    # Find versionCode
    version_code_match = re.search(r'versionCode\s*=\s*(\d+)', content)
    if not version_code_match:
        print("Could not find versionCode in build.gradle.kts")
        sys.exit(1)
    
    current_code = int(version_code_match.group(1))
    new_code = current_code + 1

    # Find versionName
    version_name_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)
    if not version_name_match:
        print("Could not find versionName in build.gradle.kts")
        sys.exit(1)

    current_name = version_name_match.group(1)
    
    # Strip any suffix like -a or similar
    base_name = current_name.split("-")[0]
    parts = base_name.split(".")
    if len(parts) != 3:
        # Fallback to 2.0.0 if it's not standard semver
        major, minor, patch = 2, 0, 0
    else:
        major, minor, patch = map(int, parts)

    if bump_type == "major":
        major += 1
        minor = 0
        patch = 0
    elif bump_type == "minor":
        minor += 1
        patch = 0
    elif bump_type == "patch":
        patch += 1

    new_name = f"{major}.{minor}.{patch}"

    # Replace in file content
    content = re.sub(r'(versionCode\s*=\s*)(\d+)', f'\\g<1>{new_code}', content)
    content = re.sub(r'(versionName\s*=\s*)"([^"]+)"', f'\\g<1>"{new_name}"', content)

    with open(file_path, "w") as f:
        f.write(content)

    print(f"Bumped version from {current_name} (code {current_code}) to {new_name} (code {new_code})")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 bump-version.py [major|minor|patch|none]")
        sys.exit(1)
    bump_type = sys.argv[1]
    if bump_type != "none":
        bump_version(bump_type)
