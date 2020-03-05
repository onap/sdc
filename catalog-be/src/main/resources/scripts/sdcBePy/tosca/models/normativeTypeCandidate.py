from sdcBePy.common.helpers import check_arguments_not_none


class NormativeTypeCandidate:

    def __init__(self, file_dir, normative_types_list):
        if not check_arguments_not_none(file_dir, normative_types_list):
            raise AttributeError("The file_dir, normative_types_list are missing")

        self.file_dir = file_dir
        self.normative_types_list = normative_types_list

    def get_parameters(self):
        return self.file_dir, self.normative_types_list
