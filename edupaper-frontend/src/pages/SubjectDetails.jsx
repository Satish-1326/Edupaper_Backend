import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

const SubjectDetails = () => {
  const { subjectId } = useParams();
  const navigate = useNavigate();

  const [subject, setSubject] = useState(null);
  const [units, setUnits] = useState([]);
  const [courseOutcomes, setCourseOutcomes] = useState([]);

  const [topics, setTopics] = useState({});

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadSubjectDetails();
  }, [subjectId]);

  const loadSubjectDetails = async () => {
    try {
      setLoading(true);
      setError("");

      const [
        subjectResponse,
        unitsResponse,
        coResponse,
      ] = await Promise.all([
        api.get(`/subjects/${subjectId}`),
        api.get(`/subjects/${subjectId}/units`),
        api.get(`/subjects/${subjectId}/course-outcomes`),
      ]);

      setSubject(subjectResponse.data);
      setUnits(unitsResponse.data);
      setCourseOutcomes(coResponse.data);

      // Load topics for every unit
      const topicResults = await Promise.all(
        unitsResponse.data.map(async (unit) => {
          try {
            const response = await api.get(
              `/units/${unit.id}/topics`
            );

            return {
              unitId: unit.id,
              topics: response.data,
            };
          } catch (error) {
            console.error(
              `Failed to load topics for unit ${unit.id}`,
              error
            );

            return {
              unitId: unit.id,
              topics: [],
            };
          }
        })
      );

      const topicMap = {};

      topicResults.forEach((item) => {
        topicMap[item.unitId] = item.topics;
      });

      setTopics(topicMap);

    } catch (error) {
      console.error(
        "Failed to load subject details:",
        error
      );

      setError(
        error.response?.data?.message ||
          "Failed to load subject details"
      );
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-100 flex items-center justify-center">
        <div className="text-slate-500 text-lg">
          Loading subject details...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-slate-100 p-8">
        <div className="max-w-5xl mx-auto">

          <button
            onClick={() => navigate("/subjects")}
            className="text-blue-600 hover:underline mb-6"
          >
            ← Back to Subjects
          </button>

          <div className="bg-red-50 border border-red-200 text-red-600 p-5 rounded-xl">
            {error}
          </div>

        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-100 p-8">

      <div className="max-w-6xl mx-auto">

        {/* Back */}
        <button
          onClick={() => navigate("/subjects")}
          className="text-blue-600 hover:underline mb-6"
        >
          ← Back to Subjects
        </button>

        {/* Subject Header */}
        <div className="bg-white rounded-2xl shadow-sm border p-8">

          <div className="flex flex-col md:flex-row md:justify-between md:items-start gap-5">

            <div>

              <p className="text-blue-600 font-semibold">
                {subject.code}
              </p>

              <h1 className="text-3xl font-bold text-slate-800 mt-1">
                {subject.name}
              </h1>

              <p className="text-slate-500 mt-2">
                {subject.description ||
                  "No description available."}
              </p>

            </div>

            <div className="flex gap-3">

              <InfoBadge
                label="Semester"
                value={subject.semester}
              />

              <InfoBadge
                label="Credits"
                value={subject.credits}
              />

            </div>

          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-8">

            <InfoItem
              label="Department"
              value={subject.department}
            />

            <InfoItem
              label="Academic Year"
              value={subject.academicYear}
            />

          </div>

        </div>

        {/* Units */}
        <section className="mt-8">

          <div className="flex justify-between items-center mb-5">

            <div>
              <h2 className="text-2xl font-bold text-slate-800">
                📚 Units
              </h2>

              <p className="text-slate-500 mt-1">
                Course units and their topics
              </p>
            </div>

            <span className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm font-semibold">
              {units.length} Units
            </span>

          </div>

          {units.length === 0 ? (

            <div className="bg-white rounded-xl border p-8 text-center text-slate-500">
              No units found.
            </div>

          ) : (

            <div className="space-y-5">

              {units.map((unit) => (

                <div
                  key={unit.id}
                  className="bg-white rounded-xl shadow-sm border overflow-hidden"
                >

                  {/* Unit Header */}
                  <div className="bg-slate-50 border-b px-6 py-5">

                    <div className="flex items-center gap-4">

                      <div className="w-11 h-11 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold">
                        {unit.unitNumber}
                      </div>

                      <div>
                        <h3 className="text-lg font-bold text-slate-800">
                          Unit {unit.unitNumber}: {unit.title}
                        </h3>

                        {unit.description && (
                          <p className="text-sm text-slate-500 mt-1">
                            {unit.description}
                          </p>
                        )}
                      </div>

                    </div>

                  </div>

                  {/* Topics */}
                  <div className="p-6">

                    <h4 className="font-semibold text-slate-700 mb-4">
                      Topics
                    </h4>

                    {!topics[unit.id] ||
                    topics[unit.id].length === 0 ? (

                      <p className="text-sm text-slate-400">
                        No topics available.
                      </p>

                    ) : (

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">

                        {topics[unit.id].map((topic) => (

                          <div
                            key={topic.id}
                            className="border rounded-lg p-4 hover:border-blue-400 transition"
                          >

                            <h5 className="font-medium text-slate-800">
                              {topic.name}
                            </h5>

                            {topic.description && (
                              <p className="text-sm text-slate-500 mt-1">
                                {topic.description}
                              </p>
                            )}

                          </div>

                        ))}

                      </div>

                    )}

                  </div>

                </div>

              ))}

            </div>

          )}

        </section>

        {/* Course Outcomes */}
        <section className="mt-10">

          <div className="flex justify-between items-center mb-5">

            <div>
              <h2 className="text-2xl font-bold text-slate-800">
                🎯 Course Outcomes
              </h2>

              <p className="text-slate-500 mt-1">
                Learning outcomes associated with this subject
              </p>
            </div>

            <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm font-semibold">
              {courseOutcomes.length} COs
            </span>

          </div>

          {courseOutcomes.length === 0 ? (

            <div className="bg-white rounded-xl border p-8 text-center text-slate-500">
              No course outcomes found.
            </div>

          ) : (

            <div className="space-y-4">

              {courseOutcomes.map((co) => (

                <div
                  key={co.id}
                  className="bg-white rounded-xl border shadow-sm p-5 flex gap-5"
                >

                  <div className="shrink-0 w-16 h-12 rounded-lg bg-green-100 text-green-700 flex items-center justify-center font-bold">
                    CO{co.coNumber}
                  </div>

                  <div>
                    <h3 className="font-semibold text-slate-800">
                      Course Outcome {co.coNumber}
                    </h3>

                    <p className="text-slate-600 mt-1">
                      {co.description}
                    </p>
                  </div>

                </div>

              ))}

            </div>

          )}

        </section>

      </div>

    </div>
  );
};


/* Information badge */

const InfoBadge = ({ label, value }) => {
  return (
    <div className="bg-slate-100 rounded-lg px-4 py-3 text-center min-w-24">

      <p className="text-xs text-slate-500">
        {label}
      </p>

      <p className="font-bold text-slate-800 mt-1">
        {value}
      </p>

    </div>
  );
};


/* Information item */

const InfoItem = ({ label, value }) => {
  return (
    <div className="bg-slate-50 rounded-lg p-4">

      <p className="text-xs text-slate-500">
        {label}
      </p>

      <p className="font-semibold text-slate-700 mt-1">
        {value || "—"}
      </p>

    </div>
  );
};

export default SubjectDetails;