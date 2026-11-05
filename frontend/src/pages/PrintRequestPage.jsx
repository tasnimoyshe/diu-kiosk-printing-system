import {
    useCallback,
    useEffect,
    useRef,
    useState
} from "react";

import {
    useNavigate
} from "react-router-dom";

import toast from "react-hot-toast";

import {
    FilePlus2,
    FileText,
    Trash2,
    Upload
} from "lucide-react";

import {
    apiErrorMessage
} from "../api/http";

import {
    publicApi
} from "../api/kioskApi";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import {
    Button,
    Card,
    Input,
    Money,
    PageHeader,
    Select
} from "../components/ui";

const INITIAL_FORM = {
    fullName: "",
    registrationNumber: "",
    userType: "STUDENT",
    department: "",
    printColor: "BLACK_AND_WHITE",
    printingSide: "SINGLE_SIDED",
    copies: 1
};

const MAX_FILES = 10;
const MAX_FILE_SIZE =
    20 * 1024 * 1024;
const MAX_TOTAL_SIZE =
    100 * 1024 * 1024;

export default function PrintRequestPage() {
    const navigate =
        useNavigate();

    const fileInputRef =
        useRef(null);

    const [form, setForm] =
        useState(INITIAL_FORM);

    const [files, setFiles] =
        useState([]);

    const [pricing, setPricing] =
        useState(null);

    const [preview, setPreview] =
        useState(null);

    const [loading, setLoading] =
        useState(false);

    const loadPricing =
        useCallback(
            async () => {
                try {
                    setPricing(
                        await publicApi.pricing()
                    );
                } catch {
                    setPricing(null);
                }
            },
            []
        );

    useEffect(
        () => {
            loadPricing();
        },
        [loadPricing]
    );

    useRealtimeInvalidation({
        topics: [
            "/topic/pricing"
        ],
        reload: loadPricing
    });

    function change(event) {
        const {
            name,
            value
        } = event.target;

        setForm(
            previous => ({
                ...previous,

                [name]:
                    name === "copies"
                        ? Number(value)
                        : value
            })
        );

        setPreview(null);
    }

    function sameFile(
        first,
        second
    ) {
        return (
            first.name === second.name
            && first.size === second.size
            && first.lastModified
            === second.lastModified
        );
    }

    function chooseFiles(event) {
        const selectedFiles =
            Array.from(
                event.target.files || []
            );

        /*
         * Clearing the native input allows a user to select the
         * same file again later after removing it.
         */
        event.target.value = "";

        if (
            selectedFiles.length === 0
        ) {
            return;
        }

        const oversized =
            selectedFiles.find(
                file =>
                    file.size
                    > MAX_FILE_SIZE
            );

        if (oversized) {
            toast.error(
                `${oversized.name} is larger than 20 MB.`
            );
            return;
        }

        const mergedFiles = [
            ...files
        ];

        let duplicateCount = 0;

        selectedFiles.forEach(
            selectedFile => {
                const duplicate =
                    mergedFiles.some(
                        existing =>
                            sameFile(
                                existing,
                                selectedFile
                            )
                    );

                if (duplicate) {
                    duplicateCount += 1;
                    return;
                }

                mergedFiles.push(
                    selectedFile
                );
            }
        );

        if (
            mergedFiles.length
            > MAX_FILES
        ) {
            toast.error(
                `Maximum ${MAX_FILES} files are allowed.`
            );
            return;
        }

        const totalSize =
            mergedFiles.reduce(
                (sum, file) =>
                    sum + file.size,
                0
            );

        if (
            totalSize
            > MAX_TOTAL_SIZE
        ) {
            toast.error(
                "Combined upload size cannot exceed 100 MB."
            );
            return;
        }

        setFiles(
            mergedFiles
        );

        setPreview(null);

        if (duplicateCount > 0) {
            toast(
                `${duplicateCount} duplicate file(s) were ignored.`
            );
        }
    }

    function removeFile(index) {
        setFiles(
            previous =>
                previous.filter(
                    (_, fileIndex) =>
                        fileIndex !== index
                )
        );

        setPreview(null);
    }

    function validate() {
        if (!form.fullName.trim()) {
            return "Full name is required.";
        }

        if (
            form.fullName.trim().length
            > 40
        ) {
            return "Full name cannot exceed 40 characters.";
        }

        if (
            !/^\d{3}-\d{2}-\d{1,6}$/
                .test(
                    form.registrationNumber.trim()
                )
        ) {
            return "Enter the DIU ID in a format such as 242-15-125.";
        }

        if (
            form.copies < 1
            || form.copies > 100
        ) {
            return "Copies must be between 1 and 100.";
        }

        if (
            files.length === 0
        ) {
            return "Select at least one document.";
        }

        if (
            files.length > MAX_FILES
        ) {
            return "Maximum 10 files are allowed.";
        }

        return null;
    }

    function requestPayload() {
        return {
            ...form,

            fullName:
                form.fullName.trim(),

            registrationNumber:
                form.registrationNumber.trim(),

            department:
                form.department.trim()
                || null
        };
    }

    async function handlePreview() {
        const error =
            validate();

        if (error) {
            toast.error(error);
            return;
        }

        setLoading(true);

        try {
            const response =
                await publicApi
                    .previewPrintRequest(
                        requestPayload(),
                        files
                    );

            setPreview(response);

            toast.success(
                "Print cost calculated."
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setLoading(false);
        }
    }

    async function handleSubmit() {
        const error =
            validate();

        if (error) {
            toast.error(error);
            return;
        }

        setLoading(true);

        try {
            const created =
                await publicApi
                    .createPrintRequest(
                        requestPayload(),
                        files
                    );

            toast.success(
                "Print request submitted successfully."
            );

            navigate(
                `/payment/${encodeURIComponent(
                    created.trackingNumber
                )}`,
                {
                    state: {
                        registrationNumber:
                        form.registrationNumber
                    }
                }
            );

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

        } finally {
            setLoading(false);
        }
    }

    const totalSelectedSize =
        files.reduce(
            (sum, file) =>
                sum + file.size,
            0
        );

    return (
        <div className="page-container">
            <PageHeader
                title="Submit Print Request"
                description="Upload your documents, choose printing options and calculate the exact cost before submitting."
            />

            <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
                <Card>
                    <div className="grid gap-4 sm:grid-cols-2">
                        <Input
                            label="Full name"
                            name="fullName"
                            maxLength={40}
                            value={form.fullName}
                            onChange={change}
                            placeholder="Your full name"
                        />

                        <Input
                            label="DIU ID / Registration number"
                            name="registrationNumber"
                            value={form.registrationNumber}
                            onChange={change}
                            placeholder="242-15-125"
                        />

                        <Select
                            label="User type"
                            name="userType"
                            value={form.userType}
                            onChange={change}
                        >
                            <option value="STUDENT">
                                Student
                            </option>

                            <option value="FACULTY">
                                Faculty
                            </option>

                            <option value="EMPLOYEE">
                                Employee
                            </option>
                        </Select>

                        <Input
                            label="Department (optional)"
                            name="department"
                            maxLength={100}
                            value={form.department}
                            onChange={change}
                            placeholder="CSE"
                        />

                        <Select
                            label="Print color"
                            name="printColor"
                            value={form.printColor}
                            onChange={change}
                        >
                            <option value="BLACK_AND_WHITE">
                                Black & White
                            </option>

                            <option value="COLOR">
                                Color
                            </option>
                        </Select>

                        <Select
                            label="Printing side"
                            name="printingSide"
                            value={form.printingSide}
                            onChange={change}
                        >
                            <option value="SINGLE_SIDED">
                                One side
                            </option>

                            <option value="DOUBLE_SIDED">
                                Both sides
                            </option>
                        </Select>

                        <Input
                            label="Number of copies"
                            name="copies"
                            type="number"
                            min={1}
                            max={100}
                            value={form.copies}
                            onChange={change}
                        />
                    </div>

                    <div className="mt-6">
              <span className="form-label">
                Documents
              </span>

                        <input
                            ref={fileInputRef}
                            type="file"
                            multiple
                            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.heic,.heif"
                            className="hidden"
                            onChange={chooseFiles}
                        />

                        {files.length === 0 ? (
                            <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 px-4 py-10 text-center">
                                <Upload className="mb-3 h-8 w-8 text-blue-600"/>

                                <span className="font-semibold">
                      Upload your documents
                    </span>

                                <span className="mt-1 text-xs text-slate-500">
                      PDF, DOC, DOCX, JPG, JPEG, PNG, HEIC or HEIF
                    </span>

                                <span className="mt-1 text-xs text-slate-500">
                      Maximum 10 files · 20 MB each · 100 MB total
                    </span>

                                <Button
                                    className="mt-5"
                                    variant="outline"
                                    onClick={
                                        () =>
                                            fileInputRef
                                                .current
                                                ?.click()
                                    }
                                >
                                    <Upload className="h-4 w-4"/>
                                    Choose Files
                                </Button>
                            </div>
                        ) : (
                            <>
                                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                    <div className="mb-4 flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                                        <div>
                                            <p className="font-semibold">
                                                Selected documents
                                            </p>

                                            <p className="text-xs text-slate-500">
                                                {files.length} / 10 files
                                                {" · "}
                                                {(totalSelectedSize / 1024 / 1024).toFixed(2)} / 100 MB
                                            </p>
                                        </div>

                                        <Button
                                            variant="outline"
                                            disabled={
                                                files.length
                                                >= MAX_FILES
                                            }
                                            onClick={
                                                () =>
                                                    fileInputRef
                                                        .current
                                                        ?.click()
                                            }
                                        >
                                            <FilePlus2 className="h-4 w-4"/>
                                            Add More Files
                                        </Button>
                                    </div>

                                    <div className="space-y-2">
                                        {files.map(
                                            (
                                                file,
                                                index
                                            ) => (
                                                <div
                                                    key={`${file.name}-${file.size}-${file.lastModified}`}
                                                    className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-3 py-3"
                                                >
                                                    <FileText className="h-5 w-5 shrink-0 text-blue-600"/>

                                                    <div className="min-w-0 flex-1">
                                                        <p className="truncate text-sm font-medium">
                                                            {file.name}
                                                        </p>

                                                        <p className="text-xs text-slate-500">
                                                            {(file.size / 1024 / 1024).toFixed(2)} MB
                                                        </p>
                                                    </div>

                                                    <button
                                                        type="button"
                                                        className="rounded-lg p-2 text-red-600 transition hover:bg-red-50"
                                                        onClick={
                                                            () =>
                                                                removeFile(
                                                                    index
                                                                )
                                                        }
                                                        aria-label={`Remove ${file.name}`}
                                                    >
                                                        <Trash2 className="h-4 w-4"/>
                                                    </button>
                                                </div>
                                            )
                                        )}
                                    </div>
                                </div>
                            </>
                        )}
                    </div>

                    <div className="mt-6 flex flex-wrap gap-3">
                        <Button
                            variant="outline"
                            disabled={loading}
                            onClick={handlePreview}
                        >
                            Calculate Cost
                        </Button>

                        <Button
                            disabled={loading}
                            onClick={handleSubmit}
                        >
                            {loading
                                ? "Processing..."
                                : "Submit Print Request"}
                        </Button>
                    </div>
                </Card>

                <div className="space-y-6">
                    <Card>
                        <h2 className="font-bold">
                            Current Printing Prices
                        </h2>

                        {pricing ? (
                            <div className="mt-4 space-y-3 text-sm">
                                <PriceLine
                                    label="B/W · One side"
                                    value={pricing.blackWhiteSingleSided}
                                />

                                <PriceLine
                                    label="Color · One side"
                                    value={pricing.colorSingleSided}
                                />

                                <PriceLine
                                    label="B/W · Both sides"
                                    value={pricing.blackWhiteDoubleSided}
                                />

                                <PriceLine
                                    label="Color · Both sides"
                                    value={pricing.colorDoubleSided}
                                />
                            </div>
                        ) : (
                            <p className="mt-3 text-sm text-slate-500">
                                Pricing is currently unavailable.
                            </p>
                        )}
                    </Card>

                    {preview && (
                        <Card className="border-blue-200">
                            <h2 className="font-bold text-blue-900">
                                Cost Preview
                            </h2>

                            <dl className="mt-4 space-y-3 text-sm">
                                <PreviewLine
                                    label="Original document pages"
                                    value={preview.totalDocumentPages}
                                />

                                <PreviewLine
                                    label="Physical sheets"
                                    value={preview.totalPhysicalSheets}
                                />

                                <PreviewLine
                                    label="Copies"
                                    value={form.copies}
                                />

                                <div className="border-t border-slate-200 pt-3">
                                    <div className="flex justify-between gap-4">
                                        <dt className="font-semibold">
                                            Total
                                        </dt>

                                        <dd className="text-lg font-bold text-blue-700">
                                            <Money
                                                value={preview.totalAmount}
                                            />
                                        </dd>
                                    </div>
                                </div>
                            </dl>
                        </Card>
                    )}
                </div>
            </div>
        </div>
    );
}

function PriceLine({
                       label,
                       value
                   }) {
    return (
        <div className="flex justify-between gap-3">
        <span className="text-slate-500">
          {label}
        </span>

            <strong>
                <Money value={value}/>
            </strong>
        </div>
    );
}

function PreviewLine({
                         label,
                         value
                     }) {
    return (
        <div className="flex justify-between gap-4">
            <dt className="text-slate-500">
                {label}
            </dt>

            <dd className="font-semibold">
                {value ?? 0}
            </dd>
        </div>
    );
}