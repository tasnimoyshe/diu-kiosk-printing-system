import {
    cp,
    mkdir,
    rm,
    stat
} from "node:fs/promises";

import {
    resolve
} from "node:path";

const frontendRoot =
    process.cwd();

const distDirectory =
    resolve(
        frontendRoot,
        "dist"
    );

const springStaticDirectory =
    resolve(
        frontendRoot,
        "../backend/src/main/resources/static"
    );

async function verifyDistDirectory() {
    try {
        const information =
            await stat(
                distDirectory
            );

        if (!information.isDirectory()) {
            throw new Error();
        }

    } catch {
        throw new Error(
            "React production build was not found. " +
            "Run 'npm run build' before running this script."
        );
    }
}

async function main() {
    await verifyDistDirectory();

    console.log(
        "Removing previous Spring Boot frontend build..."
    );

    await rm(
        springStaticDirectory,
        {
            recursive: true,
            force: true
        }
    );

    console.log(
        "Creating Spring Boot static directory..."
    );

    await mkdir(
        springStaticDirectory,
        {
            recursive: true
        }
    );

    console.log(
        "Copying React production build..."
    );

    await cp(
        distDirectory,
        springStaticDirectory,
        {
            recursive: true
        }
    );

    console.log("");
    console.log(
        "React production build copied successfully."
    );

    console.log(
        `Source: ${distDirectory}`
    );

    console.log(
        `Target: ${springStaticDirectory}`
    );

    console.log("");
    console.log(
        "Spring Boot can now serve the React application."
    );
}

main().catch(
    error => {
        console.error("");
        console.error(
            "Frontend integration failed:"
        );

        console.error(
            error.message
        );

        process.exit(1);
    }
);